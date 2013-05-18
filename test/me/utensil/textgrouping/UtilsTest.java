/**
 * 
 */
package me.utensil.textgrouping;

import java.util.ArrayList;

import junit.framework.TestCase;


/**
 * @author utensilsong
 *
 */
public class UtilsTest extends TestCase
{
    public void test_repeat()
    {
        assertEquals("", Utils.repeat('x', 0));   
        assertEquals("x", Utils.repeat('x', 1));
        assertEquals("xxxxxxx", Utils.repeat('x', 7));
        assertEquals("", Utils.repeat('x', -1));
    }
    
    public void test_containsOnly()
    {
        assertTrue(Utils.containsOnly("xxx", 'x'));
        assertFalse(Utils.containsOnly("xxyx", 'x'));
        assertFalse(Utils.containsOnly("yyyy", 'x')); 
        assertFalse(Utils.containsOnly("", 'x'));
        assertFalse(Utils.containsOnly(null, 'x'));
    }
    
    public void test_joinStrings()
    {
        assertEquals("AABB", Utils.joinStrings(TestHelper.arrayToArraylist("AA|BB".split("\\|"))));
        
        assertEquals("AAXBB", Utils.joinStrings(TestHelper.arrayToArraylist("AA|BB".split("\\|")), "X"));
        
        assertEquals("AAXBBXCC", Utils.joinStrings(TestHelper.arrayToArraylist("AA|BB|CC".split("\\|")), "X"));
        
        assertEquals("", Utils.joinStrings(null));
        
        assertEquals("", Utils.joinStrings(new ArrayList<String>()));
    }
    
    public void test_removeChars()
    {
        assertEquals("abcde", Utils.removeChars("abcde", '_'));
        
        assertEquals("abcde", Utils.removeChars("a_b_c__d_e", '_'));
        
        assertEquals("", Utils.removeChars("_____", '_'));
        
        assertEquals("", Utils.removeChars(null, '_'));
        
        assertEquals("", Utils.removeChars("", '_'));
    }
        
}
