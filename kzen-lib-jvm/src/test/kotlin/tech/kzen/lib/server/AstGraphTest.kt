package tech.kzen.lib.server


import org.junit.Assert.assertEquals
import org.junit.Test
import tech.kzen.lib.common.api.model.BundlePath
import tech.kzen.lib.common.api.model.ObjectLocation
import tech.kzen.lib.common.api.model.ObjectPath
import tech.kzen.lib.server.objects.ast.DoubleExpression
import tech.kzen.lib.server.util.GraphTestUtils


class AstGraphTest {
    //-----------------------------------------------------------------------------------------------------------------
    @Test
    fun `literal 2 + 2 = 4`() {
        val objectGraph = GraphTestUtils.newObjectGraph()

        val twoPlusTwoLocation = location("TwoPlusTwo")

        val fooNamedInstance = objectGraph.objects.get(twoPlusTwoLocation) as DoubleExpression
        assertEquals(4.0, fooNamedInstance.evaluate(), 0.0)
    }


    @Test
    fun `inline 2 + 2 = 4`() {
        val objectGraph = GraphTestUtils.newObjectGraph()

        val twoPlusTwoLocation = location("TwoPlusTwoInlineMap")

        val fooNamedInstance = objectGraph.objects.get(twoPlusTwoLocation) as DoubleExpression
        assertEquals(4.0, fooNamedInstance.evaluate(), 0.0)
    }


    //-----------------------------------------------------------------------------------------------------------------
    private fun location(name: String): ObjectLocation {
        return ObjectLocation(
                BundlePath.parse("test/nested-test.yaml"),
                ObjectPath.parse(name))
    }
}