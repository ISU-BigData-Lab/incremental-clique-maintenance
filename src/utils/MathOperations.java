package utils;

public class MathOperations {
	
	//computes base-2 logarithms.
	public static int log2(int num){
		if(num == 0)
			return 0;
		return 31 - Integer.numberOfLeadingZeros(num);
	}

}
