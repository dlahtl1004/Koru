package io.anuke.koru.components;

import io.anuke.koru.Koru;
import io.anuke.koru.entities.KoruEntity;
import io.anuke.koru.network.syncing.SyncData.Synced;

@Synced
public class FadeComponent implements KoruComponent{
	public float lifetime, life;
	
	public FadeComponent(float lifetime){
		this.lifetime = lifetime;
	}
	
	@Override
	public void update(KoruEntity entity){
		life += Koru.delta();
		if(life > lifetime){
			entity.remove();
		}
	}
	
	public float scaled(){
		return 1f-life/lifetime;
	}
	
	public FadeComponent(){}
}
