/**
 * 
 */
package me.utensil.textgrouping;

import java.util.ArrayList;

/**
 * @author utensilsong
 *
 */
public class TestHelper
{
    public static <T> ArrayList<T> arrayToArraylist(T[] array)
    {
        ArrayList<T> ret = new ArrayList<T>();
        
        for(T e : array)
        {
            ret.add(e);
        }
        
        return ret;
    }
}
