package io.anuke.koru.entities;

import com.badlogic.ashley.core.Component;

import io.anuke.koru.components.ChildComponent;
import io.anuke.koru.components.ConnectionComponent;
import io.anuke.koru.components.DamageComponent;
import io.anuke.koru.components.DataComponent;
import io.anuke.koru.components.DestroyOnTerrainHitComponent;
import io.anuke.koru.components.FadeComponent;
import io.anuke.koru.components.HealthComponent;
import io.anuke.koru.components.HitboxComponent;
import io.anuke.koru.components.InputComponent;
import io.anuke.koru.components.InventoryComponent;
import io.anuke.koru.components.ParticleComponent;
import io.anuke.koru.components.PositionComponent;
import io.anuke.koru.components.ProjectileComponent;
import io.anuke.koru.components.RenderComponent;
import io.anuke.koru.components.SyncComponent;
import io.anuke.koru.components.TextComponent;
import io.anuke.koru.components.TileComponent;
import io.anuke.koru.components.VelocityComponent;
import io.anuke.koru.network.IServer;
import io.anuke.koru.network.InputHandler;
import io.anuke.koru.network.Interpolator;
import io.anuke.koru.renderers.BlockAnimationRenderer;
import io.anuke.koru.renderers.IndicatorRenderer;
import io.anuke.koru.renderers.MonsterRenderer;
import io.anuke.koru.renderers.ParticleRenderer;
import io.anuke.koru.renderers.PlayerRenderer;
import io.anuke.koru.renderers.ProjectileRenderer;
import io.anuke.koru.systems.SyncSystem.SyncType;

public enum EntityType{
	player{
		public Component[] defaultComponents(){
			return new Component[]{new PositionComponent(), new ConnectionComponent(),
			new RenderComponent(new PlayerRenderer()), new HitboxComponent(),
			new SyncComponent(SyncType.player, new Interpolator()), new InputComponent(), 
			new HealthComponent(), new InventoryComponent(4,6)};
		}

		void initHitbox(KoruEntity entity, HitboxComponent hitbox){

			hitbox.terrainRect().set(0, 0, 6, 2);

			hitbox.entityRect().set(0, 0, 4, 5);
			hitbox.alignBottom();
			hitbox.height = 8;
		}
		
		public void deathEvent(KoruEntity entity, KoruEntity killer){
		
		}
	},
	testmob{
		public Component[] defaultComponents(){
			return new Component[]{new PositionComponent(),
			new RenderComponent(new MonsterRenderer()), 
			new HitboxComponent(), new VelocityComponent(),
			new SyncComponent(SyncType.position, new Interpolator()),
			new HealthComponent()};
		}

		void initHitbox(KoruEntity entity, HitboxComponent hitbox){

			hitbox.terrainRect().set(0, 0, 6, 2);

			hitbox.entityRect().set(0, 0, 4, 5);
			hitbox.alignBottom();
			hitbox.height = 8;
		}
		
		public void deathEvent(KoruEntity entity, KoruEntity killer){
		
		}
	},
	projectile{
		public Component[] defaultComponents(){
			return new Component[]{new PositionComponent(), new RenderComponent(new ProjectileRenderer()),
					new VelocityComponent().setDrag(0f), new HitboxComponent(), new ProjectileComponent(),
					new FadeComponent(), new DestroyOnTerrainHitComponent(), new DamageComponent()};
		}

		void initHitbox(KoruEntity entity, HitboxComponent hitbox){
			hitbox.terrainRect().set(0, 0, 4, 2);
			hitbox.entityRect().set(0, 0, 3, 3);
			hitbox.entityhitbox.setCenter(0, 4);
			hitbox.terrainhitbox.setCenter(0, 1);
			hitbox.collideterrain = true;
		}
		
		public boolean collide(KoruEntity entity, KoruEntity other){
			return entity.mapComponent(DamageComponent.class).source != other.getID();
		}
	},
	damageindicator{
		public Component[] defaultComponents(){
			return new Component[]{new PositionComponent(), new RenderComponent(new IndicatorRenderer()),
					new ChildComponent(), new TextComponent(), new FadeComponent(20).enableRender()};
		}
	},
	particle{
		public Component[] defaultComponents(){
			return new Component[]{new PositionComponent(), new RenderComponent(new ParticleRenderer()), new ParticleComponent()};
		}
	},
	blockanimation{
		public Component[] defaultComponents(){
			return new Component[]{new PositionComponent(), new RenderComponent(new BlockAnimationRenderer()), new DataComponent()};
		}
	},
	tile{
		public Component[] defaultComponents(){
			return new Component[]{new TileComponent()};
		}
	};

	final void init(KoruEntity entity){
		InputComponent input = entity.mapComponent(InputComponent.class);
		if(input != null && IServer.active()) input.input = new InputHandler(entity);

		HitboxComponent hitbox = entity.mapComponent(HitboxComponent.class);
		if(hitbox != null) initHitbox(entity, hitbox);
	}

	public boolean collide(KoruEntity entity, KoruEntity other){
		return true;
	}

	//public void collisionEvent(KoruEntity entity, KoruEntity other){
	//}
	
	public void deathEvent(KoruEntity entity, KoruEntity killer){
		entity.removeSelfServer();
	}

	void initHitbox(KoruEntity entity, HitboxComponent hitbox){
	}


	public Component[] defaultComponents(){
		return new Component[]{};
	}
}
