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
public class TextGroupTest extends TestCase
{
    public class Text implements TextGroupable<Text>
    {
        private String string;
        
        public Text(String s)
        {
            string = s;
        }

        @Override
        public String getStringForGrouping()
        {
            return string;
        }
        
    }
    
    public void test_group()
    {
        ArrayList<TextGroup<Text> > tgs = TextGroup.group(TestHelper.arrayToArraylist(new Text[] {
                new Text("processed Tag 4234545938405983538543535354 is not heard from for 17 seconds <127.0.0.1>"),
                new Text("processed Tag 4234545938405983538543535354 is not heard from for 17 seconds <127.0.0.1>"),
                new Text("process 17364 is not heard from for 17 seconds <127.0.0.2>"),
                new Text("process 12345 is not heard from for 23 seconds <127.0.0.2>"),
                new Text("process 17364 is not heard from for 23 seconds <127.0.0.1>"),
                new Text("process 17364 is not heard from for 23 seconds <127.0.0.2>"),
                new Text("process 12345 is not heard from for 17 seconds <127.0.0.1>"),
                new Text("I am alone")
                
        }));
        
        assertEquals(2, tgs.size());
        assertEquals("process___________________________________ is not heard from for __ seconds <127.0.0.__",
        		tgs.get(0).visibleCommonSubstrings());
        assertEquals("I am alone", tgs.get(1).visibleCommonSubstrings());
        
    }
}
