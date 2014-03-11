package liner2.features.tokens;

import liner2.structure.Token;
import liner2.structure.TokenAttributeIndex;

public class StartsWithLowerFeature extends TokenFeature{
	
	public StartsWithLowerFeature(String name){
		super(name);
	}
	
	public String generate(Token token, TokenAttributeIndex index){
		if (Character.isLowerCase(token.getAttributeValue(0).charAt(0)))
			return "1";
		else
			return "0";
	}
}