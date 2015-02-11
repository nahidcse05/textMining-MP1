package structures;


public class _RankItem implements Comparable<_RankItem> {
	public double m_value;
	public String m_name;
	public int m_index;
	
	public _RankItem(String name, double v) {
		m_value = v;
		m_name = name;
	}
	
	public _RankItem(int index, double v) {
		m_value = v;
		m_index = index;
	}

	@Override
	public int compareTo(_RankItem it) {
		if (this.m_value < it.m_value)
			return -1;
		else if (this.m_value > it.m_value)
			return 1;
		else
			return 0;
	}
}
