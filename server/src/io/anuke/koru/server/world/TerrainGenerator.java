package io.anuke.koru.server.world;

import static io.anuke.ucore.UCore.clamp;

import io.anuke.koru.world.Generator;
import io.anuke.koru.world.Materials;
import io.anuke.koru.world.Tile;
import io.anuke.ucore.UCore;
import io.anuke.ucore.noise.Noise;
import io.anuke.ucore.noise.RidgedPerlin;
import io.anuke.ucore.noise.VoronoiNoise;

public class TerrainGenerator implements Generator{
	final float scale = 0.9f;
	VoronoiNoise tnoise = new VoronoiNoise(0, (short) 0);
	VoronoiNoise enoise = new VoronoiNoise(10, (short) 0);
	RidgedPerlin per = new RidgedPerlin(2, 1, 0.4f);
	RidgedPerlin cper = new RidgedPerlin(3, 1, 0.4f);

	{
		enoise.setUseDistance(true);
		tnoise.setUseDistance(true);
	}

	@Override
	public Tile generate(int x, int y){
		x += 99999;
		y += 99999;

		Tile tile = new Tile();
		float riv = per.getValue(x, y + 100, 0.0005f) + Noise.nnoise(x, y, 10f, 0.01f)  + Math.abs(Noise.nnoise(x, y, 20f, 0.015f));
		float elev = getElevation(x, y) - riv/2f;
		float cave = getCaveDst(x, y) - riv/2.1f;
		float scave = getSmoothCaveDst(x, y) - riv/2.1f;
		float se = (smoothEl(x, y) + 0.4f) / 0.82f;
		
		float t = getTemperature(x, y) - riv/3f;

		if(riv > 0.23f){
			// no river edges in lakes
			if(se > 0.063f){
				tile.setMaterial(Materials.stone);
			}else{
				tile.setMaterial(Materials.water);
			}
			if(riv > 0.236)
				tile.setMaterial(Materials.water);
			if(riv > 0.244)
				tile.setMaterial(Materials.deepwater);

		}else if(elev > 0.85){
			tile.setMaterial(Materials.ice);

			if(Math.random() < 0.03)
				tile.setBlockMaterial(Materials.next(Materials.rock1, 4));

		}else if(elev > 0.76){
			tile.setMaterial(Materials.stone);

			if(Math.random() < 0.05)
				tile.setBlockMaterial(Materials.next(Materials.rock1, 4));

		}else if(se > 0.078){
			if(t < 0.62){
				if(Math.random() < 1){
					if(Math.random() < 0.02 && elev < 0.35f && elev > 0.12f)
						tile.setBlockMaterial(Materials.next(Materials.bush1, 3));
				}
				if(elev < 0.4 && elev > 0.1 && t < 0.6 && t > 0.41){
					if(Noise.snoise(x, y, 120, 13) + Noise.snoise(x, y, 5, 4) > 4)
						tile.setMaterial(Materials.shortgrassblock);

					if(Noise.snoise(x, y, 120, 13) + Noise.snoise(x, y, 5, 4) > 4.5)
						tile.setMaterial(Materials.grassblock);
				}

				if(elev > 0.36f && elev < 0.53f && Noise.snoise(x, y, 500, 30) + Noise.snoise(x, y, 9, 4) > 3){
					if(rand() < br(0.12f, elev)){
						if(rand() < 0.026)
							tile.setMaterial(Materials.next(Materials.oaktree1, 6));
						if(rand() < 0.02)
							tile.setMaterial(Materials.next(Materials.mushy1, 8));
						if(Math.random() < 0.05)
							tile.setBlockMaterial(Materials.next(Materials.tallgrass1, 3));
					}
				}
				if((elev > 0.5f && Math.random() < clamp((elev - 0.5f) * 3f) / 15f)){
					tile.setMaterial(Materials.next(Materials.pinetree1, 4));
					if(Math.random() < 0.04)
						tile.setMaterial(Materials.next(Materials.rock1, 4));

					if(Math.random() < 0.002)
						tile.setBlockMaterial(Materials.pinesapling);
				}

				if(t < 0.4){
					tile.setMaterial(Materials.darkgrass);
				}else{
					tile.setMaterial(Materials.grass);
				}
			}else if(t < 0.8){
				tile.setMaterial(Materials.burntgrass);
				if(Math.random() < 0.03)
					tile.setBlockMaterial(Materials.next(Materials.drybush1, 3));
				if(Math.random() < 0.003)
					tile.setBlockMaterial(Materials.next(Materials.deadtree1, 4));

				if(Math.random() < 0.01)
					tile.setBlockMaterial(Materials.next(Materials.rock1, 4));
				if(Math.random() < 0.1)
					tile.setBlockMaterial(Materials.next(Materials.tallgrass1, 3));

			}else if(t < 0.83){
				tile.setMaterial(Materials.burntgrass2);
				if(Math.random() < 0.04)
					tile.setBlockMaterial(Materials.next(Materials.tallgrass1, 3));
				if(Math.random() < 0.001)
					tile.setBlockMaterial(Materials.next(Materials.deadtree1, 4));
				if(Math.random() < 0.001)
					tile.setBlockMaterial(Materials.next(Materials.burnedtree1, 4));
			}else{
				tile.setMaterial(Materials.sand);
				if(Math.random() < 0.02)
					tile.setBlockMaterial(Materials.next(Materials.rock1, 4));

			}

		}else{
			if(t < 0.3){
				tile.setMaterial(Materials.darkgrass);
			}else{
				tile.setMaterial(Materials.grass);
			}
			if(se < 0.063f){
				tile.setMaterial(Materials.water);
				if(se < 0.054)
					tile.setMaterial(Materials.deepwater);
			}else if(se < 0.066){
				tile.setMaterial(Materials.stone);
				if(Math.random() < 0.01)
					tile.setMaterial(Materials.next(Materials.rock1, 4));
				if(Math.random() < 0.005)
					tile.setMaterial(Materials.next(Materials.rock1, 4));
			}else{
				if(Math.random() < 0.006 && se > 0.069)
					tile.setMaterial(Materials.next(Materials.willowtree1, 4));
			}
		}

		if(cave > 0.7){
			if(riv < 0.23f){
				tile.setMaterial(Materials.stoneblock);
				tile.setMaterial(Materials.stone);

				if(Noise.nnoise(x, y, 100, 0.5f) + Noise.nnoise(x, y, 15, 1) + Noise.nnoise(x, y, 50, 1)
						+ cper.getValue(x, y, 0.005f) > 0.19){
					tile.setMaterial(Materials.air);
				}
			}
			if(riv < 0.24)
			tile.setLight(clamp(1f - (scave - 0.7f) * 60f));
		}else if(cave > 0.69){
			if(riv < 0.23f){
				tile.setMaterial(Materials.stone);
				tile.setMaterial(Materials.air);

				if(Math.random() < 0.05)
					tile.setBlockMaterial(Materials.next(Materials.rock1, 4));
			}
		}

		return tile;
	}

	float br(float e, float b){
		return clamp(1f - Math.abs(e - b));
	}

	public float getElevation(int x, int y){
		x += 999999;
		y += 999999;

		double elevation = 0.4f;
		float octave = 1200f * scale;

		elevation += smoothEl(x, y);
		elevation += (Noise.nnoise(x, y, octave / 128, 0.125f));
		elevation += (Noise.nnoise(x, y, octave / 32, 0.125f / 4));
		elevation += enoise.noise(x, y, 1 / 1000.0) / 3.4;

		elevation /= 0.84;

		elevation = UCore.clamp(elevation);

		return (float) elevation;

	}

	public float smoothEl(int x, int y){
		double elevation = 0f;
		float octave = 1200f * scale;

		elevation += (Noise.nnoise(x, y, octave, 1f));
		elevation += (Noise.nnoise(x, y, octave / 2, 0.5f));
		elevation += (Noise.nnoise(x, y, octave / 4, 0.25f));
		elevation += (Noise.nnoise(x, y, octave / 8, 0.125f));
		elevation += (Noise.nnoise(x, y, octave / 16, 0.125f / 2));
		elevation += (Noise.nnoise(x, y, octave / 32, 0.125f / 4));
		elevation += (Noise.nnoise(x, y, octave / 128, 0.125f / 16));

		return (float) elevation;
	}

	public float getTemperature(int x, int y){
		x += 99999 * 2;
		y += 99999 * 2;

		double temp = 0.5f;
		float octave = 800f;

		temp += (Noise.nnoise(x, y, octave, 1f));
		temp += (Noise.nnoise(x, y, octave / 2, 0.5f));
		temp += (Noise.nnoise(x, y, octave / 4, 0.25f));
		temp += (Noise.nnoise(x, y, octave / 8, 0.125f));
		// temp += (Noise.nnoise(x, y, octave/16, 0.25f));
		temp += (Noise.nnoise(x, y, octave / 32, 0.125f));
		temp += (Noise.nnoise(x, y, octave / 64, 0.125f / 2));
		temp += (Noise.nnoise(x, y, octave / 128, 0.125f / 2));
		temp += tnoise.noise(x, y, 1 / 1000.0) / 3.4;

		temp /= 1.05;

		temp = UCore.clamp(temp);

		return (float) temp;
	}

	public float getCaveDst(int x, int y){
		x += 99999 * 3;
		y += 99999 * 3;

		double out = 0.5f;
		float octave = 800f;

		out += (Noise.nnoise(x, y, octave, 1f));
		out += (Noise.nnoise(x, y, octave / 2, 0.5f));
		out += (Noise.nnoise(x, y, octave / 4, 0.25f));
		out += (Noise.nnoise(x, y, octave / 64, 0.125f / 2));
		out += tnoise.noise(x, y, 1 / 1000.0) / 3.1;

		out /= 1.05;

		out = UCore.clamp(out);

		return (float) out;
	}
	
	public float getSmoothCaveDst(int x, int y){
		x += 99999 * 3;
		y += 99999 * 3;

		double out = 0.5f;
		float octave = 800f;

		out += (Noise.nnoise(x, y, octave, 1f));
		out += (Noise.nnoise(x, y, octave / 2, 0.5f));
		out += (Noise.nnoise(x, y, octave / 4, 0.25f));
		out += (Noise.nnoise(x, y, octave / 64, 0.125f / 2.5f));
		out += tnoise.noise(x, y, 1 / 1000.0) / 3.1;

		out /= 1.05;

		out = UCore.clamp(out);

		return (float) out;
	}

	double rand(){
		return Math.random();
	}
}
