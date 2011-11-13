package steven.utility;

import java.util.ArrayList;
import java.util.List;

public class StringUtility{
	public static String[] split(final String p, final String token){
		if(p == null){
			return null;
		}
		String tmp = p;
		int index = -1;
		final List<String> segments = new ArrayList<String>();
		while((index = tmp.indexOf(token)) >= 0){
			segments.add(tmp.substring(0,index));
			tmp = tmp.substring(index + 1);
		}
		segments.add(tmp);
		return segments.toArray(new String[segments.size()]);
	}
}
