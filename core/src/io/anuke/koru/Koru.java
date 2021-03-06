package io.anuke.koru;

import java.util.Calendar;

import com.badlogic.gdx.Gdx;

import io.anuke.koru.modules.*;
import io.anuke.koru.network.IServer;
import io.anuke.koru.systems.CollisionDebugSystem;
import io.anuke.koru.systems.KoruEngine;
import io.anuke.ucore.UCore;
import io.anuke.ucore.modules.ModuleController;
import io.anuke.ucore.util.ColorCodes;

public class Koru extends ModuleController<Koru>{
	private static Koru instance;
	private static StringBuffer log = new StringBuffer();
	public KoruEngine engine;

	@Override
	public void init(){
		instance = this;
		engine = new KoruEngine();

		addModule(Network.class);
		addModule(Renderer.class);
		addModule(Input.class);
		addModule(ClientData.class);
		addModule(World.class);
		addModule(ObjectHandler.class);
		addModule(UI.class);
		
		engine.addSystem(new CollisionDebugSystem());
	}

	@Override
	public void render(){

		try{
			engine.update(Gdx.graphics.getDeltaTime()*60f);
			super.render();
		}catch(Exception e){
			e.printStackTrace();
			
			//write log
			Gdx.files.local("korulog-" + Calendar.getInstance().getTime() + ".log").writeString(UCore.parseException(e), false);
			//exit, nothing left to do here
			Gdx.app.exit();
		}

	}
	
	public static float delta(){
		return IServer.active() ? IServer.instance().getDelta() : Gdx.graphics.getDeltaTime()*60f;
	}

	public static void log(Object o){
		StackTraceElement e = Thread.currentThread().getStackTrace()[2];
		String name = e.getFileName().replace(".java", "");

		if(IServer.active() || Gdx.app == null){
			if(Gdx.app == null){
				System.out.println(ColorCodes.BACK_DEFAULT + ColorCodes.BOLD + ColorCodes.LIGHT_BLUE + "[" + name + "]: "
						+ ColorCodes.LIGHT_GREEN + o + ColorCodes.RED);
			}else{
				Gdx.app.log(
						ColorCodes.BACK_DEFAULT + ColorCodes.LIGHT_BLUE + "[" + ColorCodes.BLUE + name + ColorCodes.BACK_DEFAULT + "::"
								+ ColorCodes.LIGHT_YELLOW + e.getMethodName() + ColorCodes.LIGHT_BLUE + "]",
						ColorCodes.LIGHT_GREEN + "" + o + ColorCodes.RED);
			}
		}else{
			Gdx.app.log("[" + name + "::" + e.getMethodName() + "]", "" + o);

			String message = "[" + name + "::" + e.getMethodName() + "]" + "" + o;
			log.append(message + "\n");
			int l = 500;
			
			
			log = new StringBuffer(log.substring(Math.max(0, log.length()-l), log.length()));
		}

		if(o instanceof Exception){
			((Exception) o).printStackTrace();
		}
	}

	public static CharSequence getLog(){
		return log;
	}

	public static KoruEngine getEngine(){
		return instance.engine;
	}
}
