package io.anuke.koru.network.packets;

import com.badlogic.gdx.math.Vector2;

import io.anuke.koru.network.syncing.SyncData.Synced;

@Synced //<-- why is this here? nobody knows
public class PositionPacket{
	public float x, y, mouseangle;
	public int direction;
	public Vector2 velocity;
}
