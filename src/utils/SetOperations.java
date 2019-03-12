package utils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class SetOperations<T>
{
    
    public static<T> HashSet<T> intersect(Collection<T> A, Collection<T>B){
        
        HashSet<T> R = new HashSet<T>();
        
        if(A.size() >= B.size()){
            for(T b : B){
                if(A.contains(b)){
                    R.add(b);
                }
            }
        }
        else{
            for(T a : A){
                if(B.contains(a)){
                    R.add(a);
                }
            }
        }
        return R;
        
    }
    
   /* public static<T> Collection<T> parIntersect(Collection<T> A, Collection<T>B){
        
        Collection<T> R = ConcurrentHashMap.newKeySet();
        
        if(A.size() >= B.size()){
        	
        	B.parallelStream().forEach(b -> {
        		if(A.contains(b))
        			R.add(b);
        	});
        }
        else{
        	
        	A.parallelStream().forEach(a -> {
        		if(B.contains(a))
        			R.add(a);
        	});
        }
        return R;
        
    }*/
    
    //http://chrisdblades.com/java-8-collection-intersection/
    public static<T> Collection<T> parIntersect(Collection<T> A, Collection<T> B){
    	BiFunction<Collection<T>, Collection<T>, Collection<T>> intersector = (smallest, biggest) ->
    														smallest.parallelStream()
    														.filter((e) -> biggest.contains(e))
    														.collect(Collectors.toList());
    														
    	if(null == A || null == B) {
    		return Collections.emptyList();
    	} else {
    		return A.size() < B.size() ? intersector.apply(A, B) : intersector.apply(B, A);
    	}
    }
    

    public static void main(String[] args)
    {
        // TODO Auto-generated method stub

    }

}
