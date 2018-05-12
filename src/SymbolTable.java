import java.util.ArrayList;

/**
 * symbol과 관련된 데이터와 연산을 소유한다. section 별로 하나씩 인스턴스를 할당한다.
 */
public class SymbolTable
{
	ArrayList<String> symbolList;
	ArrayList<Integer> locationList;
	ArrayList<Integer> modifSizeList;
	// 기타 literal, external 선언 및 처리방법을 구현한다.

	public SymbolTable()
	{
		symbolList = new ArrayList<>();
		locationList = new ArrayList<>();
		modifSizeList = new ArrayList<>();
	}

	/**
	 * 새로운 Symbol을 table에 추가한다.
	 * 
	 * @param symbol
	 *            : 새로 추가되는 symbol의 label
	 * @param location
	 *            : 해당 symbol이 가지는 주소값 <br>
	 * 			<br>
	 *            주의 : 만약 중복된 symbol이 putSymbol을 통해서 입력된다면 이는 프로그램 코드에 문제가 있음을 나타낸다.
	 *            매칭되는 주소값의 변경은 modifySymbol()을 통해서 이루어져야 한다.
	 */
	public void putSymbol(String symbol, int location)
	{
		String inputSymbol = symbol;

		if (symbol.contains("="))
			inputSymbol = inputSymbol.replaceAll("=", "");

		if (!symbolList.contains(inputSymbol))
		{
			symbolList.add(inputSymbol);
			locationList.add(location);
		}
	}
	
	public void putModifSymbol(String modifSymbol, int location, int modifSize)
	{
		symbolList.add(modifSymbol);
		locationList.add(location);
		modifSizeList.add(modifSize);
	}

	/**
	 * 기존에 존재하는 symbol 값에 대해서 가리키는 주소값을 변경한다.
	 * 
	 * @param symbol
	 *            : 변경을 원하는 symbol의 label
	 * @param newLocation
	 *            : 새로 바꾸고자 하는 주소값
	 */
	public void modifySymbol(String symbol, int newLocation)
	{
		String inputSymbol = symbol;

		if (symbol.contains("="))
			inputSymbol = inputSymbol.replaceAll("=", "");

		if (symbolList.contains(inputSymbol))
		{
			for (int index = 0; index < symbolList.size(); index++)
				if (inputSymbol.equals(symbolList.get(index)))
				{
					symbolList.set(index, inputSymbol);
					locationList.set(index, newLocation);
					break;
				}
		}
	}

	/**
	 * 인자로 전달된 symbol이 어떤 주소를 지칭하는지 알려준다.
	 * 
	 * @param symbol
	 *            : 검색을 원하는 symbol의 label
	 * @return symbol이 가지고 있는 주소값. 해당 symbol이 없을 경우 -1 리턴
	 */
	public int search(String symbol)
	{
		int address = 0;
		// ...
		if (symbolList.contains(symbol))
		{
			for (int index = 0; index < symbolList.size(); index++)
				if (symbol.equals(symbolList.get(index)))
				{
					address = locationList.get(index);
					break;
				}
		}
		else
			address = -1;

		return address;
	}

	public String getSymbol(int index)
	{
		return symbolList.get(index);
	}

	public int getLocation(int index)
	{
		return locationList.get(index);
	}

	public int getSize()
	{
		return symbolList.size();
	}
	
	public int getLiteralSize(int index)
	{
		int size = 0;
		
		if (symbolList.get(index).contains("X"))
		{
			size = 1;
		}
		else if (symbolList.get(index).contains("C"))
		{
			String literal = symbolList.get(index).replaceAll("C|\'", "");
			size = literal.length();
		}
		
		return size;
	}
	
	public int getModifSize(int index)
	{
		return modifSizeList.get(index);
	}
}
