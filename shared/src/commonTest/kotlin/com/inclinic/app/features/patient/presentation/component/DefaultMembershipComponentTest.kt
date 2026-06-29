@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.inclinic.app.features.patient.presentation.component

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.inclinic.app.core.model.CardToken
import com.inclinic.app.core.model.RawCard
import com.inclinic.app.core.model.Subscription
import com.inclinic.app.core.model.SubscriptionTier
import com.inclinic.app.core.port.CardTokenizer
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.patient.infrastructure.remote.SubscriptionDataSource
import com.inclinic.app.features.patient.subscription.application.GetSubscriptionUseCase
import com.inclinic.app.features.patient.subscription.application.PurchasePremiumUseCase
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

private class FakeSubscriptionDataSource(
    private val getResult: Result<Subscription> = Result.success(Subscription(SubscriptionTier.FREE, null)),
    private val purchaseResult: Result<Subscription> = Result.success(
        Subscription(SubscriptionTier.PREMIUM, "2026-07-19T00:00:00.000Z"),
    ),
    val purchaseCalls: MutableList<Pair<String, String>> = mutableListOf(),
) : SubscriptionDataSource {
    override suspend fun getSubscription(): Result<Subscription> = getResult
    override suspend fun purchasePremium(cardToken: String, paymentMethodId: String): Result<Subscription> {
        purchaseCalls += cardToken to paymentMethodId
        return purchaseResult
    }
}

private class FakeMembershipCardTokenizer(
    private val result: Result<CardToken> = Result.success(CardToken(token = "tok-123", last4 = "4242", brand = "visa")),
) : CardTokenizer {
    override suspend fun tokenize(card: RawCard): Result<CardToken> = result
}

class DefaultMembershipComponentTest {

    private val dispatchers = TestAppDispatchers()
    private val lifecycle = LifecycleRegistry().also { it.resume() }
    private val ctx = DefaultComponentContext(lifecycle = lifecycle)

    private fun createComponent(
        dataSource: SubscriptionDataSource = FakeSubscriptionDataSource(),
        cardTokenizer: CardTokenizer = FakeMembershipCardTokenizer(),
        outputs: MutableList<MembershipComponent.Output> = mutableListOf(),
    ): DefaultMembershipComponent = DefaultMembershipComponent(
        componentContext = ctx,
        getSubscription = GetSubscriptionUseCase(dataSource, dispatchers),
        purchasePremium = PurchasePremiumUseCase(cardTokenizer, dataSource, dispatchers),
        dispatchers = dispatchers,
        onOutput = outputs::add,
    )

    private fun fillValidCard(component: DefaultMembershipComponent) {
        component.onCardNumberChange("4111111111111111")
        component.onExpiryChange("12/27")
        component.onCvvChange("123")
        component.onCardholderNameChange("Ana Torres")
    }

    @Test
    fun initial_load_sets_tier_from_datasource() = runTest {
        val ds = FakeSubscriptionDataSource(
            getResult = Result.success(Subscription(SubscriptionTier.PREMIUM, "2026-07-19T00:00:00.000Z")),
        )
        val component = createComponent(dataSource = ds)

        val state = component.state.value
        assertFalse(state.isLoading)
        assertEquals(SubscriptionTier.PREMIUM, state.tier)
        assertEquals("2026-07-19T00:00:00.000Z", state.expiresAt)
        assertTrue(state.isPremium)
    }

    @Test
    fun initial_load_free_keeps_free_tier() = runTest {
        val component = createComponent()

        assertEquals(SubscriptionTier.FREE, component.state.value.tier)
        assertFalse(component.state.value.isPremium)
    }

    @Test
    fun load_failure_sets_error() = runTest {
        val ds = FakeSubscriptionDataSource(getResult = Result.failure(Exception("boom")))
        val component = createComponent(dataSource = ds)

        assertFalse(component.state.value.isLoading)
        assertNotNull(component.state.value.error)
    }

    @Test
    fun onUpgradeTapped_shows_checkout() = runTest {
        val component = createComponent()

        component.onUpgradeTapped()

        assertTrue(component.state.value.showCheckout)
    }

    @Test
    fun onSubmitPurchase_success_flips_tier_to_premium_and_closes_checkout() = runTest {
        val ds = FakeSubscriptionDataSource()
        val component = createComponent(dataSource = ds)
        component.onUpgradeTapped()
        fillValidCard(component)

        component.onSubmitPurchase()

        val state = component.state.value
        assertEquals(SubscriptionTier.PREMIUM, state.tier)
        assertEquals("2026-07-19T00:00:00.000Z", state.expiresAt)
        assertFalse(state.isPurchasing)
        assertFalse(state.showCheckout)
        assertNull(state.error)
        assertEquals(1, ds.purchaseCalls.size)
        assertEquals("tok-123" to "visa", ds.purchaseCalls.first())
    }

    @Test
    fun onSubmitPurchase_failure_sets_error_and_keeps_free() = runTest {
        val ds = FakeSubscriptionDataSource(purchaseResult = Result.failure(Exception("Pago 402")))
        val component = createComponent(dataSource = ds)
        component.onUpgradeTapped()
        fillValidCard(component)

        component.onSubmitPurchase()

        val state = component.state.value
        assertEquals(SubscriptionTier.FREE, state.tier)
        assertFalse(state.isPurchasing)
        assertNotNull(state.error)
    }

    @Test
    fun onSubmitPurchase_invalid_card_sets_error_without_calling_datasource() = runTest {
        val ds = FakeSubscriptionDataSource()
        val component = createComponent(dataSource = ds)
        component.onUpgradeTapped()
        component.onCardNumberChange("1234")
        component.onExpiryChange("12/27")
        component.onCvvChange("123")
        component.onCardholderNameChange("Ana Torres")

        component.onSubmitPurchase()

        assertNotNull(component.state.value.error)
        assertTrue(ds.purchaseCalls.isEmpty())
    }

    @Test
    fun onErrorDismissed_clears_error() = runTest {
        val ds = FakeSubscriptionDataSource(getResult = Result.failure(Exception("boom")))
        val component = createComponent(dataSource = ds)
        assertNotNull(component.state.value.error)

        component.onErrorDismissed()

        assertNull(component.state.value.error)
    }

    @Test
    fun onBack_emits_NavigateBack() = runTest {
        val outputs = mutableListOf<MembershipComponent.Output>()
        val component = createComponent(outputs = outputs)

        component.onBack()

        assertEquals(1, outputs.size)
        assertTrue(outputs.first() is MembershipComponent.Output.NavigateBack)
    }
}
