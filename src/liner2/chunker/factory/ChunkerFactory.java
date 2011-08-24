package liner2.chunker.factory;

import java.util.ArrayList;

import liner2.Main;
import liner2.chunker.Chunker;

public class ChunkerFactory {

	private static ChunkerFactory factory = null;
	private ArrayList<ChunkerFactoryItem> items = null;
	
	public ChunkerFactory(){
		this.items = new ArrayList<ChunkerFactoryItem>();
//		this.items.add(new ChunkerFactoryItemHeuristics());
//		this.items.add(new ChunkerFactoryItemGazetteers());
//		this.items.add(new ChunkerFactoryItemLingpipeTrain());
//		this.items.add(new ChunkerFactoryItemNLTKTrain());
//		this.items.add(new ChunkerFactoryItemNLTKLoad());
		this.items.add(new ChunkerFactoryItemCRFPPTrain());
		this.items.add(new ChunkerFactoryItemCRFPPLoad());
//		this.items.add(new ChunkerFactoryItemRescore());
	}
	
	/**
	 * Get current ChunkerFactory. If the factory does not exist then create it.
	 * @return
	 */
	public static ChunkerFactory get(){
		if ( ChunkerFactory.factory == null )
			ChunkerFactory.factory = new ChunkerFactory();
		return ChunkerFactory.factory;
	}
	
	/**
	 * Get human-readable description of chunker commands.
	 * @return
	 */
	public String getDescription(){
		StringBuilder sb = new StringBuilder();
		for (ChunkerFactoryItem item : this.items)
			sb.append("  " + item.getPattern() + "\n");
		return sb.toString();
	}
	
	/**
	 * Creates a chunker according to the description
	 * @param description
	 * @return
	 * @throws Exception 
	 */
	public Chunker createChunker(String description) throws Exception{
		Main.log("-> Setting up chunker: " + description);
		
		if (true)
			for (ChunkerFactoryItem item : this.items)
				if ( item.getPattern().matcher(description).find() )
					return item.getChunker(description);
				
		return null;
	}
	
	/**
	 * Validate a chunker description.
	 * @param description
	 * @return
	 */
	public boolean parse(String description){
		for (ChunkerFactoryItem item : this.items)
			if (item.getPattern().matcher(description).find())
				return true;
		return false;
	}
}
