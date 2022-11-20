import java.io.*;

public nctclass count {

	public static void main(String[] args) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			String s = reader.readLine();
			
			int left = 0;
			int right = 0;
			int forward = 0;
			int backwards = 0;
			
			while(s != null) {
				if(s.startsWith("I'm going forward")) {
					forward++;
				} else if(s.startsWith("I'm going left")) {
					left++;
				} else if(s.startsWith("I'm going right")) {
					right++;
				} else if(s.startsWith("I'm going backwards")) {
					backwards++;
				} else {
					System.out.println("Unknown [" + s + "]");
				}
				
				s = reader.readLine();
				System.out.println("Summary of moves: Forward=" + forward +" Left=" +
						left + " Right=" + right + " Backwards=" + backwards);
			}
			
		} catch(Exception e) {
			// some kind of horrible error.
			System.err.println("Error: " + e.getMessage());
			e.printStackTrace();
		}
	}

}
