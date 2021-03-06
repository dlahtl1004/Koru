package io.anuke.koru.entities.types;

import io.anuke.koru.components.*;
import io.anuke.koru.entities.ComponentList;
import io.anuke.koru.entities.EntityType;
import io.anuke.koru.entities.KoruEntity;
import io.anuke.koru.renderers.EnemyRenderer;
import io.anuke.koru.systems.SyncSystem.SyncType;

public class TestEntity extends EntityType{

	@Override
	public ComponentList components(){
		return list(new PositionComponent(),
				new RenderComponent(new EnemyRenderer()), 
				new ColliderComponent(),
				new SyncComponent(SyncType.physics),
				new HealthComponent());
	}
	
	public void init(KoruEntity entity){
		entity.collider().collider.drag = 0.4f;
		entity.collider().collider.setSize(7, 7);
	}

}
