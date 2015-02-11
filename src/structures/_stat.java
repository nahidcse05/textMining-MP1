/**
 * 
 */
package structures;


/**
 * @author lg5bt
 * basic statistics for each feature
 */
public class _stat {
	int[] m_DF; // document frequency for this feature
	int[] m_TTF; // total term frequency for this feature
	
	public _stat(int classNo){
		m_DF = new int[classNo];
		m_TTF = new int[classNo];
	}
	
	public int[] getDF(){
		return this.m_DF;
	}
	
	public int[] getTTF(){
		return this.m_TTF;
	}
	
	//The DF(The document frequency) of a feature is added by one.
	public void addOneDF(int index){
		this.m_DF[index]++;
	}
	
	//The TTF(Total term frequency) of a feature is added by one.
	public void addOneTTF(int index){
		this.m_TTF[index]++;
	}
}


