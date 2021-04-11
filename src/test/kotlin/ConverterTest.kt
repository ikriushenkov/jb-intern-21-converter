import exceptions.SyntaxException
import exceptions.TypeException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ConverterTest {
    val shorten = Parser.Companion::shorten

    val simplify = Parser.Companion::simplify

    @Test
    fun testExample() {
        assertEquals("filter{((element>10)&(element<20))}%>%map{element}",
            shorten("filter{(element>10)}%>%filter{(element<20)}"))
        assertEquals("filter{((element+10)>10)}%>%map{((element+10)*(element+10))}",
            shorten("map{(element+10)}%>%filter{(element>10)}%>%map{(element*element)}"))
    }

    @Test
    fun testEmpty() {
        assertEquals("filter{(0=0)}%>%map{element}",
            shorten(""))
    }

    @Test
    fun testOnlyFilter() {
        assertEquals("filter{(((element=1)&(element<20))&(element>-1))}%>%map{element}",
            shorten("filter{(element=1)}%>%filter{(element<20)}%>%filter{(element>-1)}"))
        assertEquals("filter{(((element>1)&(element>20))&(element>-1))}%>%map{element}",
            shorten("filter{(element>1)}%>%filter{(element>20)}%>%filter{(element>-1)}"))
    }

    @Test
    fun testOnlyMap() {
        assertEquals("filter{(0=0)}%>%map{element}", shorten("map{element}"))
        assertEquals("filter{(0=0)}%>%map{element}", shorten("map{element}%>%map{element}"))
        assertEquals("filter{(0=0)}%>%map{((element-1)*2)}",
            shorten("map{(element-1)}%>%map{(element*2)}"))
        assertEquals("filter{(0=0)}%>%map{(element>1)}", shorten("map{(element>1)}"))
    }

    @Test
    fun testSimplify() {
        assertEquals("filter{(0=0)}%>%map{0}", simplify("map{(1-1)}"))
        assertEquals("filter{(0=0)}%>%map{-1926}", simplify("map{((1-10)*(256-42))}"))
        assertEquals("filter{(0=0)}%>%map{0}", simplify("map{(element-element)}"))
        assertEquals("filter{(0=0)}%>%map{((element*2)-2)}",
            simplify("map{(element-1)}%>%map{(element*2)}"))
        assertEquals("filter{(0=0)}%>%map{(element-1)}",
            simplify("map{(element+1)}%>%map{(element-2)}"))
        assertEquals("filter{(0=0)}%>%map{0}", simplify("map{(element*0)}"))
        assertEquals("filter{(0=0)}%>%map{(element*3)}", simplify("map{((element*2)+element)}"))
        assertEquals("filter{(0=0)}%>%map{(element*-122)}", simplify("map{((element*-123)+element)}"))
        assertEquals("filter{(0=0)}%>%map{(element*122)}", simplify("map{((element*123)-element)}"))
        assertEquals("filter{(0=0)}%>%map{((element*element)*123)}",
            simplify("map{((element*123)*element)}"))
    }

    @Test
    fun testSyntaxException() {
        assertFailsWith<SyntaxException> { shorten("map{1+10}") }
        assertFailsWith<SyntaxException> { shorten("map{(el+1)}") }
        assertFailsWith<SyntaxException> { shorten("map{(1+1") }
        assertFailsWith<SyntaxException> { shorten("map{}") }
        assertFailsWith<SyntaxException> { shorten("hello") }
        assertFailsWith<SyntaxException> { shorten("filter{((element>=10)&(element<20))}%>%map{element}") }
        assertFailsWith<SyntaxException> { shorten("filter{map{element}}") }
    }

    @Test
    fun testTypeException() {
        assertFailsWith<TypeException> { shorten("filter{5}") }
        assertFailsWith<TypeException> { shorten("filter{element}") }
        assertFailsWith<TypeException> { shorten("filter{(1&2)}") }
        assertFailsWith<TypeException> { shorten("map{(1&2)}") }
        assertFailsWith<TypeException> { shorten("filter{((0=0)>(1=0))}") }
        assertFailsWith<TypeException> { shorten("filter{(element|element)}") }
    }
}