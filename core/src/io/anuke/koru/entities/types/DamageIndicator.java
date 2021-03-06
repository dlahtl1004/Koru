package io.anuke.koru.entities.types;

import io.anuke.koru.components.*;
import io.anuke.koru.entities.ComponentList;
import io.anuke.koru.entities.EntityType;
import io.anuke.koru.renderers.IndicatorRenderer;

public class DamageIndicator extends EntityType{

	@Override
	public ComponentList components(){
		return list(new PositionComponent(), new RenderComponent(new IndicatorRenderer()),
				new ChildComponent(), new TextComponent(), new FadeComponent(20));
	}

}
