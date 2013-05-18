/**
 * 
 */
package me.utensil.textgrouping;

/**
 * {@link #TextGroup }
 * only accepts element types that implemented this interface
 * 
 * @author utensilsong
 *
 */
public interface TextGroupable<T> {
    
    /**
     * get string for text grouping
     * 
     * @return the string that requires {@link #TextGroup } to recognize its pattern
     */
    public String getStringForGrouping();
}
