/**
 * 
 */
package me.utensil.textgrouping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.regex.Pattern;

import name.fraser.neil.plaintext.diff_match_patch;
import name.fraser.neil.plaintext.diff_match_patch.Diff;

/**
 * A class to group texts that has patterns by recognizing their patterns
 * 
 * @author utensilsong
 * 
 * TODO thread safety
 *
 */
public class TextGroup<T extends TextGroupable<T> > {
    
    /**
     * meta char to hold position for differences, 
     * so the diff algorithm will not have misunderstandings due to position mismatches.
     * 
     * it should never actually appear in the input strings.
     */
	public static char META_CHAR = '‡å';	
	
	/**
	 * use this char for logs instead of {@link #META_CHAR} to make it more readable
	 */
	public static char META_CHAR_VIS = '_';
	
	/**
	 * minimal size of the same part of two strings to be actually considered as "the same"
	 */
	public static int SAME_MIN_SIZE = 2;
	
	public static int ADAPT_DIFF_TOLERANCE = 3;
	
	/**
	 * minimal similarity for two strings to be considered as "similar"
	 */
	public static double SIMILARITY_THRESHOLD = 0.8;
	
	/**
	 * same as {@link #commonSubstringsOf(String, String, int)}
	 * 
	 * with {@code min_size} default to {@code SAME_MIN_SIZE} which is 2
	 */
	public static ArrayList<String> commonSubstringsOf(String s, String t)
	{
		return commonSubstringsOf(s, t, SAME_MIN_SIZE);
	}
	
	/**
	 * get the common substrings of {@code s} and {@code t},
	 * but it doesn't just include the same parts but also place holders(using {@link #META_CHAR} ) 
	 * for the different part
	 * 
	 * @param s string 1
	 * @param t string 2
	 * @param min_size minimal size of the same part of two strings to be actually considered as "the same"
	 */
	public static ArrayList<String> commonSubstringsOf(String s, String t, int min_size)
	{
		diff_match_patch dmp = new diff_match_patch();
		
		LinkedList<Diff> diffs = dmp.diff_main(s, t);
		
		dmp.diff_cleanupSemanticLossless(diffs);
		
		ArrayList<String> all_substrings = new ArrayList<String>();
		
		Diff last_diff = null;
		
		int last_diff_size = 0;
		
		for(Diff diff : diffs)
		{			
			String current_text = diff.text;
			diff_match_patch.Operation op = diff.operation;
			
			// an equal
			if(op == diff_match_patch.Operation.EQUAL)
			{
				// flush the current difference before this equal if any				
				if(last_diff_size > 0)
				{
					//hold the positions for the difference
					all_substrings.add(Utils.repeat(META_CHAR, last_diff_size));
					last_diff_size = 0;
				}				
				
				// the equal part must be bigger than min_size
				// and it must not contain only META_CHAR's
				if(diff.text.length() > min_size 
						&& !Utils.containsOnly(current_text, META_CHAR))
				{
					all_substrings.add(current_text);
				}				
				// else considered to be a difference
				else
				{
					//hold the positions for the difference
					all_substrings.add(Utils.repeat(META_CHAR, current_text.length()));
				}
			}
			// a differnece
			else
			{
				// fresh start
				if(
					// beginning of string	
					last_diff == null
					// end of an equal
					|| last_diff.operation == diff_match_patch.Operation.EQUAL
					// end of a paired insert/delete ops
					|| last_diff_size == 0)
				{
					last_diff_size = current_text.length();
				}
				else
				{
					//for the successfully paired insert/delete ops between equals, 
					//take the bigger difference size
					//and flush immediately
					if(last_diff.operation != diff.operation)
					{
						last_diff_size = Math.max(last_diff_size, current_text.length());
						//hold the positions for the difference
						all_substrings.add(Utils.repeat(META_CHAR, last_diff_size));
						last_diff_size = 0;
					}
					//consecutive non-equal op?! accumulate then
					else
					{
						last_diff_size += current_text.length();
					}					
				}
			}		
			
			last_diff = diff;
		}
		
		return all_substrings;		
	}
	
	protected ArrayList<T> items;
	
	protected ArrayList<String> groupCommonSubstrings;
	
	protected Pattern guessedRegex;
	
	protected boolean mergedByOtherGroup;
	
	/**
     * @return the items
     */
    public ArrayList<T> getItems()
    {
        return items;
    }
    
    /**
     * @return the guessed regex
     */
    public Pattern getGuessedRegex()
    {
        return guessedRegex;
    }

    /**
     * constructor.
     * 
     * a text group must be constructed with a first item, there must be no empty {@link #TextGroup} 
     * unless it {@link #isMergedByOtherGroup()}.
     * 
     * @param first_item
     */
    public TextGroup(T first_item)
	{
	    items = new ArrayList<T>();
	    
	    mergedByOtherGroup = false;
	    
	    accept(first_item);
	    
	    Utils.debug("%s has become a group", first_item.getStringForGrouping());
	}
    
    /**
     * just make common substrings visible for logging, using {@link #META_CHAR_VIS} to replace {@link #META_CHAR}
     */
    protected static String visualize(Iterable<String> common_substrings)
    {
        if(common_substrings == null) return "";
        
        return Utils.joinStrings(common_substrings).replace(META_CHAR, META_CHAR_VIS);
    }
    
    /**
     * get a visible common substrings of the group for logging, 
     * using {@link #META_CHAR_VIS} to replace {@link #META_CHAR}
     */
    public String visibleCommonSubstrings()
    {
    	if(groupCommonSubstrings == null)
    	{
    		return items.get(0).getStringForGrouping();
    	}
    	else
    	{
    		return visualize(groupCommonSubstrings);
    	}
        
    }
    
    /**
     * get a common substrings of the group for comparing/calculating similarity with other group
     */
    public String comparableCommonSubstrings()    
    {
        if(groupCommonSubstrings == null)
        {
            return items.get(0).getStringForGrouping();
        }
        else
        {
            return Utils.joinStrings(groupCommonSubstrings);
        }       
    }
    
    /**
     * the essential of common substrings is the part that are actually the same
     * 
     * see {@link #commonSubstringsOf(String, String, int)} to understand
     * why there is such an unnecessary
     */
    protected static String essentialOf(Iterable<String> common_substrings)
    {
        if(common_substrings == null) return "";
        
        return Utils.removeChars(Utils.joinStrings(common_substrings), META_CHAR);
    }
    
    /**
     * a group must be marked 'merged' after merging it
     */
    public void markMergedByOtherGroup()
    {
        items.clear();
        
        mergedByOtherGroup = true;        
    }
    
    /** 
     * check if it has been merged by another group
     */
    public boolean isMergedByOtherGroup()
    {
        return mergedByOtherGroup || items.size() == 0;
    }

    /**
     * check if it should not be merged by {@code group} morally :P
     */
    public boolean shouldNotMerge(TextGroup<T> group)
    {
        return this.equals(group) || isMergedByOtherGroup() || group.isMergedByOtherGroup();
    }
    
    /**
     * try to accept an {@code item} by calculating the similarity
     * 
     * @param item
     * @return true if accepted, false otherwise
     */
    public boolean tryToAccept(T item)
    {
    	if(isMergedByOtherGroup()) return false;
    	
        String item_string =  item.getStringForGrouping();
        
        // the pattern of the group already clear
        if(guessedRegex != null && guessedRegex.matcher(item_string).matches())
        {       
            Utils.debug("%s is accepted by regex: [%s]", item_string, guessedRegex.pattern());
            accept(item);
        }
        // meet a potfriend
        else
        {
            String ccs = comparableCommonSubstrings();
            
            ArrayList<String> common_substrings = commonSubstringsOf(ccs, item_string);
            
            //# the group can adapt to accept you
            if((essentialOf(common_substrings).length() + 0.0) / (ccs.length() + 0.0) > SIMILARITY_THRESHOLD)
            {                
                Utils.debug("%s is accepted by adapting %s to %s", 
                        item_string, visibleCommonSubstrings(), visualize(common_substrings)); 
                
                accept(item);
                adaptTo(common_substrings);                
            }
            //# sorry, you are not my friend
            else
            {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * try to merge a {@code group} by calculating the similarity
     * 
     * @param group
     * @return true if merged, false otherwise
     */
    public boolean tryToMerge(TextGroup<T> group)
    {
        if(shouldNotMerge(group)) return false;
        
        if(guessedRegex != null && group.getGuessedRegex() != null && guessedRegex == group.getGuessedRegex())
        {
            merge(group);
            return true;
        }
        // the larger merges the smaller
        else if(group.getItems().size() > items.size())
        {
            return group.tryToMerge(this);
        }
        else
        {
            String ccs = comparableCommonSubstrings();
            String gccs = group.comparableCommonSubstrings();
            
            ArrayList<String> common_substrings = commonSubstringsOf(ccs, gccs);
            
            // can adapt to merge you
            if((essentialOf(common_substrings).length() + 0.0) / (ccs.length() + 0.0) > SIMILARITY_THRESHOLD)
            {
                Utils.debug("%s is merged by adapting %s to %s", 
                        gccs, visibleCommonSubstrings(), visualize(common_substrings)); 
                
                merge(group);
                adaptTo(common_substrings);
                return true;
            }
            //# sorry, you are not my friend
            else
            {
            	Utils.debug("%s can't merged %s", 
            			visibleCommonSubstrings(), group.visibleCommonSubstrings()); 
                return false;
            }  
        }
    }

    /**
     * actually accept an {@code item}
     * @param item
     */
    protected void accept(T item)
    {
        items.add(item);
        //TODO mark item to be belong to this group
        //TODO guess a regex
    }
    
    /**
     * adapt the {@link #groupCommonSubstrings} to {@code new_common}
     * for accepting new member
     */
    protected void adaptTo(ArrayList<String> new_common)
    {        
        //# adapting to wider parameter
        if(groupCommonSubstrings != null)
        {
            groupCommonSubstrings.clear();
        }
        
        groupCommonSubstrings = new_common;
    }
    
    /**
     * actually merge a {@code group}
     * @param group
     */
    protected void merge(TextGroup<T> group)
    {
        if(shouldNotMerge(group)) return;
        
        for(T item : group.getItems())
        {
            items.add(item);
        }
        
        group.markMergedByOtherGroup();
    } 
    
    /**
     * group the text_groupable_items according to their similarity
     * 
     * @param text_groupable_items
     * @return
     */
    public static <T extends TextGroupable<T> > ArrayList<TextGroup<T> > group(Iterable<T> text_groupable_items)
    {
        ArrayList<TextGroup<T> > groups = new ArrayList<TextGroup<T> >();
        
        //first round: accept
        for(T text_groupable_item : text_groupable_items)
        {
            TextGroup<T> accepted_group = null;
            
            for(TextGroup<T> group : groups)
            {
                if(group.tryToAccept(text_groupable_item))
                {
                    accepted_group = group;
                }
            }
            
            //# no one accepts me, I am alone
            if(accepted_group == null)
            {
                groups.add(new TextGroup<T>(text_groupable_item));
            }
        }
        
        //second round: merge
        for(TextGroup<T> group : groups)
        {
            
            if(!group.isMergedByOtherGroup())
            {
                for(TextGroup<T> other_group : groups)
                {
                    if(!other_group.isMergedByOtherGroup())
                    {
                        group.tryToMerge(other_group);
                    }
                }
            }            
            
        }
        
        //final round: clean up
        for(Iterator<TextGroup<T> > it = groups.iterator(); it.hasNext();)
        {
            TextGroup<T> group = it.next();
            
            //cleanup
            if(group.isMergedByOtherGroup())
            {
                //Note that Iterator.remove is the only safe way to modify a collection
                //during iteration; the behavior is unspecified if the underlying collection
                //is modified in any other way while the iteration is in progress.
                //http://docs.oracle.com/javase/tutorial/collections/interfaces/collection.html
                it.remove();
            }
        }
        
        return groups;
    }
	
}
