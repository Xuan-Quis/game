package com.tutorial.androidgametutorial.helpers;

import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;

import com.tutorial.androidgametutorial.entities.Building;
import com.tutorial.androidgametutorial.entities.Character;
import com.tutorial.androidgametutorial.entities.GameCharacters;
import com.tutorial.androidgametutorial.entities.GameObject;
import com.tutorial.androidgametutorial.entities.Player;
import com.tutorial.androidgametutorial.entities.enemies.Boom;
import com.tutorial.androidgametutorial.entities.enemies.Skeleton;
import com.tutorial.androidgametutorial.entities.items.Item;
import com.tutorial.androidgametutorial.entities.items.Items;
import com.tutorial.androidgametutorial.environments.Doorway;
import com.tutorial.androidgametutorial.environments.GameMap;
import com.tutorial.androidgametutorial.environments.Tiles;

import java.util.ArrayList;

public class HelpMethods {


    public static PointF CreatePointForDoorway(GameMap gameMapLocatedIn, int buildingIndex) {
        Building building = gameMapLocatedIn.getBuildingArrayList().get(buildingIndex);

        float x = building.getPos().x;
        float y = building.getPos().y;
        PointF point = gameMapLocatedIn.getBuildingArrayList().get(buildingIndex).getBuildingType().getDoorwayPoint();

        return new PointF(point.x + x, point.y + y - building.getBuildingType().getHitboxRoof());

    }

    public static PointF CreatePointForDoorway(int xTile, int yTile) {
//        float x = xTile * GameConstants.Sprite.SIZE;
//        float y = yTile * GameConstants.Sprite.SIZE;
//
//        return new PointF(x + 1, y + 1);

        float x = xTile * GameConstants.Sprite.SIZE + GameConstants.Sprite.SIZE / 2f;
        float y = yTile * GameConstants.Sprite.SIZE + GameConstants.Sprite.SIZE / 2f;

        return new PointF(x, y);
    }

    public static void ConnectTwoDoorways(GameMap gameMapOne, PointF pointOne, GameMap gameMapTwo, PointF pointTwo) {

        Doorway doorwayOne = new Doorway(pointOne, gameMapOne);
        Doorway doorwayTwo = new Doorway(pointTwo, gameMapTwo);

        doorwayOne.connectDoorway(doorwayTwo);
        doorwayTwo.connectDoorway(doorwayOne);
    }


    public static ArrayList<Skeleton> GetSkeletonsRandomized(int amount, int[][] gameMapArray) {

        int width = (gameMapArray[0].length - 1) * GameConstants.Sprite.SIZE;
        int height = (gameMapArray.length - 1) * GameConstants.Sprite.SIZE;

        ArrayList<Skeleton> skeletonArrayList = new ArrayList<>();

        for (int i = 0; i < amount; i++) {
            float x = (float) (Math.random() * width);
            float y = (float) (Math.random() * height);
            skeletonArrayList.add(new Skeleton(new PointF(x, y), GameCharacters.SKELETON));
        }
        return skeletonArrayList;
    }

    public static ArrayList<Boom> GetBoomsRandomized(int amount, int[][] gameMapArray) {
        int width = (gameMapArray[0].length - 1) * GameConstants.Sprite.SIZE;
        int height = (gameMapArray.length - 1) * GameConstants.Sprite.SIZE;

        ArrayList<Boom> boomArrayList = new ArrayList<>();

        for (int i = 0; i < amount; i++) {
            float x = (float) (Math.random() * width);
            float y = (float) (Math.random() * height);
            boomArrayList.add(new Boom(new PointF(x, y)));
        }

        return boomArrayList;
    }

    public static float MoveNextToTileUpDown(RectF hitbox, float cameraY, float deltaY) {
        int currentTile;
        int playerPosY;
        float cameraYReturn;

        if (deltaY > 0) {
            playerPosY = (int) (hitbox.top - cameraY);
            currentTile = playerPosY / GameConstants.Sprite.SIZE;
            cameraYReturn = hitbox.top - (currentTile * GameConstants.Sprite.SIZE);
        } else {
            playerPosY = (int) (hitbox.bottom - cameraY);
            currentTile = playerPosY / GameConstants.Sprite.SIZE;
            cameraYReturn = hitbox.bottom - (currentTile * GameConstants.Sprite.SIZE) - (GameConstants.Sprite.SIZE - 1);
        }

        return cameraYReturn;
    }

    public static float MoveNextToTileLeftRight(RectF hitbox, float cameraX, float deltaX) {
        int currentTile;
        int playerPosX;
        float cameraXReturn;

        if (deltaX > 0) {
            playerPosX = (int) (hitbox.left - cameraX);
            currentTile = playerPosX / GameConstants.Sprite.SIZE;
            cameraXReturn = hitbox.left - (currentTile * GameConstants.Sprite.SIZE);
        } else {
            playerPosX = (int) (hitbox.right - cameraX);
            currentTile = playerPosX / GameConstants.Sprite.SIZE;
            cameraXReturn = hitbox.right - (currentTile * GameConstants.Sprite.SIZE) - (GameConstants.Sprite.SIZE - 1);
        }

        return cameraXReturn;
    }

    public static boolean CanWalkHere(float x, float y, GameMap gameMap) {
        if (x < 0 || y < 0)
            return false;

        if (x >= gameMap.getMapWidth() || y >= gameMap.getMapHeight())
            return false;

        int tileX = (int) (x / GameConstants.Sprite.SIZE);
        int tileY = (int) (y / GameConstants.Sprite.SIZE);

        int tileId = gameMap.getSpriteID(tileX, tileY);

        return IsTileWalkable(tileId, gameMap.getFloorType());
    }

    public static boolean CanWalkHereUpDown(RectF hitbox, float deltaY, float currentCameraX, GameMap gameMap) {
        if (hitbox.top + deltaY < 0) return false;
        else if (hitbox.bottom + deltaY >= gameMap.getMapHeight()) return false;

        if (gameMap.getGameObjectArrayList() != null) {
            RectF tempHitbox = new RectF(hitbox.left + currentCameraX, hitbox.top + deltaY, hitbox.right + currentCameraX, hitbox.bottom + deltaY);
            for (GameObject go : gameMap.getGameObjectArrayList()) {
                if (RectF.intersects(go.getHitbox(), tempHitbox))
                    return false;
            }
        }

        //Buildings
        if (gameMap.getBuildingArrayList() != null) {
            RectF tempHitbox = new RectF(hitbox.left + currentCameraX, hitbox.top + deltaY, hitbox.right + currentCameraX, hitbox.bottom + deltaY);
            for (Building b : gameMap.getBuildingArrayList())
                if (RectF.intersects(b.getHitbox(), tempHitbox))
                    return false;
        }

        Point[] tileCords = GetTileCords(hitbox, currentCameraX, deltaY);
        int[] tileIds = GetTileIds(tileCords, gameMap);

        return IsTilesWalkable(tileIds, gameMap.getFloorType());
    }

    public static boolean CanWalkHereLeftRight(RectF hitbox, float deltaX, float currentCameraY, GameMap gameMap) {
        if (hitbox.left + deltaX < 0) return false;
        else if (hitbox.right + deltaX >= gameMap.getMapWidth()) return false;

        if (gameMap.getGameObjectArrayList() != null) {
            RectF tempHitbox = new RectF(hitbox.left + deltaX, hitbox.top + currentCameraY, hitbox.right + deltaX, hitbox.bottom + currentCameraY);
            for (GameObject go : gameMap.getGameObjectArrayList()) {
                if (RectF.intersects(go.getHitbox(), tempHitbox))
                    return false;
            }
        }

        //Buildings
        if (gameMap.getBuildingArrayList() != null) {
            RectF tempHitbox = new RectF(hitbox.left + deltaX, hitbox.top + currentCameraY, hitbox.right + deltaX, hitbox.bottom + currentCameraY);
            for (Building b : gameMap.getBuildingArrayList())
                if (RectF.intersects(b.getHitbox(), tempHitbox))
                    return false;
        }

        Point[] tileCords = GetTileCords(hitbox, deltaX, currentCameraY);
        int[] tileIds = GetTileIds(tileCords, gameMap);

        return IsTilesWalkable(tileIds, gameMap.getFloorType());
    }


    public static boolean CanWalkHere(RectF hitbox, float deltaX, float deltaY, GameMap gameMap) {
        if (hitbox.left + deltaX < 0 || hitbox.top + deltaY < 0) return false;
        else if (hitbox.right + deltaX >= gameMap.getMapWidth()) return false;
        else if (hitbox.bottom + deltaY >= gameMap.getMapHeight()) return false;

        //Objects
        if (gameMap.getGameObjectArrayList() != null) {
            RectF tempHitbox = new RectF(hitbox.left + deltaX, hitbox.top + deltaY, hitbox.right + deltaX, hitbox.bottom + deltaY);
            for (GameObject go : gameMap.getGameObjectArrayList()) {
                if (RectF.intersects(go.getHitbox(), tempHitbox))
                    return false;
            }
        }

        //Buildings
        if (gameMap.getBuildingArrayList() != null) {
            RectF tempHitbox = new RectF(hitbox.left + deltaX, hitbox.top + deltaY, hitbox.right + deltaX, hitbox.bottom + deltaY);
            for (Building b : gameMap.getBuildingArrayList())
                if (RectF.intersects(b.getHitbox(), tempHitbox))
                    return false;
        }

        Point[] tileCords = GetTileCords(hitbox, deltaX, deltaY);
        int[] tileIds = GetTileIds(tileCords, gameMap);

        return IsTilesWalkable(tileIds, gameMap.getFloorType());
    }

    // ========== HÀM RIÊNG CHO MAP NGOÀI - KHÔNG KIỂM TRA BIÊN MAP ==========
    public static boolean CanWalkHereOutside(RectF hitbox, float deltaX, float deltaY, GameMap gameMap) {
        // CHỈ KIỂM TRA VA CHẠM VỚI OBJECTS VÀ BUILDINGS - KHÔNG KIỂM TRA BIÊN MAP

        //Objects
        if (gameMap.getGameObjectArrayList() != null) {
            RectF tempHitbox = new RectF(hitbox.left + deltaX, hitbox.top + deltaY, hitbox.right + deltaX, hitbox.bottom + deltaY);
            for (GameObject go : gameMap.getGameObjectArrayList()) {
                if (RectF.intersects(go.getHitbox(), tempHitbox))
                    return false;
            }
        }

        //Buildings
        if (gameMap.getBuildingArrayList() != null) {
            RectF tempHitbox = new RectF(hitbox.left + deltaX, hitbox.top + deltaY, hitbox.right + deltaX, hitbox.bottom + deltaY);
            for (Building b : gameMap.getBuildingArrayList())
                if (RectF.intersects(b.getHitbox(), tempHitbox))
                    return false;
        }

        // KHÔNG KIỂM TRA TILE KHI RA NGOÀI BIÊN MAP để tránh ArrayIndexOutOfBoundsException
        // Chỉ kiểm tra tile nếu vẫn trong biên map
        if (hitbox.left + deltaX >= 0 && hitbox.top + deltaY >= 0 &&
            hitbox.right + deltaX < gameMap.getMapWidth() && hitbox.bottom + deltaY < gameMap.getMapHeight()) {
            Point[] tileCords = GetTileCords(hitbox, deltaX, deltaY);
            int[] tileIds = GetTileIds(tileCords, gameMap);
            return IsTilesWalkable(tileIds, gameMap.getFloorType());
        }

        // Nếu ra ngoài biên map thì cho phép di chuyển (để trigger game over sau)
        return true;
    }

    public static boolean CanWalkHereUpDownOutside(RectF hitbox, float deltaY, float currentCameraX, GameMap gameMap) {
        // CHỈ KIỂM TRA VA CHẠM VỚI OBJECTS VÀ BUILDINGS - KHÔNG KIỂM TRA BIÊN MAP

        //Objects
        if (gameMap.getGameObjectArrayList() != null) {
            RectF tempHitbox = new RectF(hitbox.left, hitbox.top + deltaY, hitbox.right, hitbox.bottom + deltaY);
            for (GameObject go : gameMap.getGameObjectArrayList()) {
                if (RectF.intersects(go.getHitbox(), tempHitbox))
                    return false;
            }
        }

        //Buildings
        if (gameMap.getBuildingArrayList() != null) {
            RectF tempHitbox = new RectF(hitbox.left, hitbox.top + deltaY, hitbox.right, hitbox.bottom + deltaY);
            for (Building b : gameMap.getBuildingArrayList())
                if (RectF.intersects(b.getHitbox(), tempHitbox))
                    return false;
        }

        // KHÔNG KIỂM TRA TILE KHI RA NGOÀI BIÊN MAP để tránh ArrayIndexOutOfBoundsException
        // Chỉ kiểm tra tile nếu vẫn trong biên map
        if (hitbox.top + deltaY >= 0 && hitbox.bottom + deltaY < gameMap.getMapHeight()) {
            Point[] tileCords = GetTileCords(hitbox, 0, deltaY);
            int[] tileIds = GetTileIds(tileCords, gameMap);
            return IsTilesWalkable(tileIds, gameMap.getFloorType());
        }

        // Nếu ra ngoài biên map thì cho phép di chuyển (để trigger game over sau)
        return true;
    }

    public static boolean CanWalkHereLeftRightOutside(RectF hitbox, float deltaX, float currentCameraY, GameMap gameMap) {
        // CHỈ KIỂM TRA VA CHẠM VỚI OBJECTS VÀ BUILDINGS - KHÔNG KIỂM TRA BIÊN MAP

        //Objects
        if (gameMap.getGameObjectArrayList() != null) {
            RectF tempHitbox = new RectF(hitbox.left + deltaX, hitbox.top, hitbox.right + deltaX, hitbox.bottom);
            for (GameObject go : gameMap.getGameObjectArrayList()) {
                if (RectF.intersects(go.getHitbox(), tempHitbox))
                    return false;
            }
        }

        //Buildings
        if (gameMap.getBuildingArrayList() != null) {
            RectF tempHitbox = new RectF(hitbox.left + deltaX, hitbox.top, hitbox.right + deltaX, hitbox.bottom);
            for (Building b : gameMap.getBuildingArrayList())
                if (RectF.intersects(b.getHitbox(), tempHitbox))
                    return false;
        }

        // KHÔNG KIỂM TRA TILE KHI RA NGOÀI BIÊN MAP để tránh ArrayIndexOutOfBoundsException
        // Chỉ kiểm tra tile nếu vẫn trong biên map
        if (hitbox.left + deltaX >= 0 && hitbox.right + deltaX < gameMap.getMapWidth()) {
            Point[] tileCords = GetTileCords(hitbox, deltaX, 0);
            int[] tileIds = GetTileIds(tileCords, gameMap);
            return IsTilesWalkable(tileIds, gameMap.getFloorType());
        }

        // Nếu ra ngoài biên map thì cho phép di chuyển (để trigger game over sau)
        return true;
    }


    private static int[] GetTileIds(Point[] tileCords, GameMap gameMap) {
        int[] tileIds = new int[4];

        for (int i = 0; i < tileCords.length; i++)
            tileIds[i] = gameMap.getSpriteID(tileCords[i].x, tileCords[i].y);

        return tileIds;
    }


    private static Point[] GetTileCords(RectF hitbox, float deltaX, float deltaY) {
        Point[] tileCords = new Point[4];

        int left = (int) ((hitbox.left + deltaX) / GameConstants.Sprite.SIZE);
        int right = (int) ((hitbox.right + deltaX) / GameConstants.Sprite.SIZE);
        int top = (int) ((hitbox.top + deltaY) / GameConstants.Sprite.SIZE);
        int bottom = (int) ((hitbox.bottom + deltaY) / GameConstants.Sprite.SIZE);

        tileCords[0] = new Point(left, top);
        tileCords[1] = new Point(right, top);
        tileCords[2] = new Point(left, bottom);
        tileCords[3] = new Point(right, bottom);

        return tileCords;

    }

    public static boolean IsTilesWalkable(int[] tileIds, Tiles tilesType) {
        for (int i : tileIds)
            if (!(IsTileWalkable(i, tilesType)))
                return false;
        return true;
    }

    public static boolean IsTileWalkable(int tileId, Tiles tilesType) {
        if (tilesType == Tiles.INSIDE)
            return (tileId == 394 || tileId < 374);

        return true;
    }

    public static boolean IsPlayerCloseForAttack(Character character, Player player, float cameraY, float cameraX) {
        float xDelta = character.getHitbox().left - (player.getHitbox().left - cameraX);
        float yDelta = character.getHitbox().top - (player.getHitbox().top - cameraY);

        float distance = (float) Math.hypot(xDelta, yDelta);

        return distance < GameConstants.Sprite.SIZE * 1.5f;
    }
    
    // Drop items khi giết quái
    public static Item tryDropItem(PointF enemyPosition) {
        double random = Math.random();
        
        if (random < 0.3) { // 30% cơ hội drop MEDIPACK
            return new Item(Items.MEDIPACK, new PointF(enemyPosition.x, enemyPosition.y));
        } else if (random < 0.6) { // 30% cơ hội drop FISH
            return new Item(Items.FISH, new PointF(enemyPosition.x, enemyPosition.y));
        } else {
            // 40% cơ hội drop HP (sử dụng EMPTY_POT làm HP item)
            return new Item(Items.EMPTY_POT, new PointF(enemyPosition.x, enemyPosition.y));
        }
    }
}
