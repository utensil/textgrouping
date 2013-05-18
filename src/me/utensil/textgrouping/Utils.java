/**
 * 
 */
package me.utensil.textgrouping;

import java.util.Arrays;
import java.util.Iterator;

/**
 * @author utensilsong
 *
 */
public class Utils {
	
	/**
	 * generates a string that repeats {@code c} for {@code n} times
	 */
	public static String repeat(char c, int n)
	{
	    if(n <= 0) return "";
	    
		char[] repeated = new char[n];
		
		Arrays.fill(repeated, c);
		
		return new String(repeated);
	}
	
	/**
	 * check if String {@code s} contains only {@code c}
	 */
	public static boolean containsOnly(String s, char c)
	{
	    if(s == null || s.length() == 0) return false;
	    
		for(int i = 0, len = s.length(); i < len; ++i)
		{
			if(s.charAt(i) != c)
			{
				return false;
			}
		}
		return true;
	}
	
	/**
	 * same as {@link #joinStrings(Iterable, String)}, and default {@code sep} to empty string
	 */
	public static String joinStrings(Iterable<String> list)
	{
		return joinStrings(list, "");
	}	
	
	/**
	 * join a list of string to a string with {@code sep}.
	 * 
	 * @param list the string list
	 * @param sep the separator 
	 */
	public static String joinStrings(Iterable<String> list, String sep)
	{
	    if(list == null) return "";
	    
		StringBuffer sb = new StringBuffer();
		
		for(Iterator<String> it = list.iterator(); it.hasNext();)
		{
			sb.append(it.next());
			
			if(it.hasNext())
			{
			    sb.append(sep);
			}			
		}
		
		return sb.toString();
	}
	
	/**
     * remove all char {@code c} from string {@code s}
     */
    public static String removeChars(String s, char c)
    {
        if(s == null || s.length() == 0) return "";
        
        StringBuffer r = new StringBuffer( s.length() );
        r.setLength( s.length() );
        int current = 0;
        for (int i = 0; i < s.length(); i ++) {
           char cur = s.charAt(i);
           if (cur != c) r.setCharAt( current++, cur );
        }
        r.setLength(current);
        
        return r.toString();
    }
	
	/**
	 * log to standard error for debuging purpose, 
	 * parameters work the same way as {@code String.format()}
	 */
	public static void debug(String fmt, Object... objs)
	{
	    System.err.println(String.format(fmt, objs));
	}
}
