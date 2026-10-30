package tech.skot.core.components

import android.widget.AdapterView
import android.widget.AutoCompleteTextView
import android.widget.FrameLayout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import tech.skot.core.components.inputs.SKComboVC
import tech.skot.core.components.inputs.SKComboViewProxy
import tech.skot.view.tests.SKTestView
import tech.skot.view.tests.testComponent
import tech.skot.viewlegacy.R

class TestSKComboAdapter : SKTestView() {
    // A ListView can still ask the adapter for its previously selected row after the choices
    // shrank (AbsListView.handleDataChanged in touch mode), see Crashlytics Argos a34175e5.
    @Test
    fun adapterToleratesAStalePositionAfterTheChoicesShrink() {
        val combo =
            SKComboViewProxy(
                choicesInitial =
                    listOf(
                        SKComboVC.Choice("choice 1"),
                        SKComboVC.Choice("choice 2"),
                    ),
            )

        testComponent(combo, duration = 0) { scenario ->
            combo.choices = listOf(SKComboVC.Choice("choice 1"))
            scenario.onActivity { activity ->
                val adapter = activity.findViewById<AutoCompleteTextView>(R.id.autoComplete).adapter
                assertEquals(1, adapter.count)
                assertEquals(AdapterView.INVALID_ROW_ID, adapter.getItemId(1))
                assertNull(adapter.getItem(1))
                assertNotNull(adapter.getView(1, null, FrameLayout(activity)))
            }
        }
    }
}
