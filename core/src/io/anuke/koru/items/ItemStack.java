package io.anuke.koru.items;

public class ItemStack implements Cloneable{
	public Item item;
	public int amount;
	
	public ItemStack(Item item, int amount){
		this.item = item;
		this.amount = amount;
	}
	
	public ItemStack(Item item){
		this(item, 1);
	}
	
	public ItemStack(ItemStack stack){
		this(stack.item, stack.amount);
	}
	
	public ItemStack(){
		
	}
	
	public void set(Item item, int amount){
		this.item = item;
		this.amount = amount;
	}
	
	public ItemStack clone(){
		return new ItemStack(this);
	}
	
	public String toString(){
		return item.name() + "x" + amount;
	}
}
