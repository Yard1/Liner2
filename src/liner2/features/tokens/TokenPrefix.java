package liner2.features.tokens;

import liner2.structure.Token;

public class TokenPrefix extends ATokenFeature {

	private int index = 0;
	private int prefixLength = 0;
	
	/**
	 * 	
	 * @param index — feature index for which the prefix will be generated,
	 * @param prefixLength — prefix length in characters.
	 */
	public TokenPrefix(String name, int index, int prefixLength){
		super(name);
		this.index = index;
		this.prefixLength = prefixLength;
	}
	
	@Override
	public String generate(Token token) {
		String value = token.getAttributeValue(this.index);
		int n = Math.min(this.prefixLength, value.length()-1);
		return value.substring(0, n);
	}

	
}
