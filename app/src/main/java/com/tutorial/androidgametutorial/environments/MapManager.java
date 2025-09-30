package com.tutorial.androidgametutorial.environments;

import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.RectF;

import com.tutorial.androidgametutorial.entities.Building;
import com.tutorial.androidgametutorial.entities.Buildings;
import com.tutorial.androidgametutorial.entities.GameObject;
import com.tutorial.androidgametutorial.entities.GameObjects;
import com.tutorial.androidgametutorial.entities.enemies.Boom;
import com.tutorial.androidgametutorial.entities.enemies.Monster;
import com.tutorial.androidgametutorial.entities.enemies.Skeleton;
import com.tutorial.androidgametutorial.entities.items.Item;
import com.tutorial.androidgametutorial.entities.items.Items;
import com.tutorial.androidgametutorial.gamestates.Playing;
import com.tutorial.androidgametutorial.helpers.GameConstants;
import com.tutorial.androidgametutorial.helpers.HelpMethods;
import com.tutorial.androidgametutorial.main.MainActivity;

import java.util.ArrayList;

public class MapManager {

    private GameMap currentMap;
    private float cameraX, cameraY;
    private Playing playing;

    public MapManager(Playing playing) {
        this.playing = playing;
        initTestMap();
    }

    public void setCameraValues(float cameraX, float cameraY) {
        this.cameraX = cameraX;
        this.cameraY = cameraY;
    }

    public boolean canMoveHere(float x, float y) {
        if (x < 0 || y < 0)
            return false;

        if (x >= getMaxWidthCurrentMap() || y >= getMaxHeightCurrentMap())
            return false;

        return true;
    }

    public int getMaxWidthCurrentMap() {
        return currentMap.getArrayWidth() * GameConstants.Sprite.SIZE;
    }

    public int getMaxHeightCurrentMap() {
        return currentMap.getArrayHeight() * GameConstants.Sprite.SIZE;
    }

    public void drawObject(Canvas c, GameObject go) {
        c.drawBitmap(go.getObjectType().getObjectImg(),
                go.getHitbox().left + cameraX,
                go.getHitbox().top - go.getObjectType().getHitboxRoof() + cameraY,
                null);
    }

    public void drawBuilding(Canvas c, Building b) {
        c.drawBitmap(b.getBuildingType().getHouseImg(),
                b.getPos().x + cameraX,
                b.getPos().y - b.getBuildingType().getHitboxRoof() + cameraY,
                null);
    }

    public void drawTiles(Canvas c) {
        for (int j = 0; j < currentMap.getArrayHeight(); j++)
            for (int i = 0; i < currentMap.getArrayWidth(); i++)
                c.drawBitmap(currentMap.getFloorType().getSprite(currentMap.getSpriteID(i, j)),
                        i * GameConstants.Sprite.SIZE + cameraX,
                        j * GameConstants.Sprite.SIZE + cameraY,
                        null);
    }

    public void drawItem(Canvas c, Item item) {
        c.drawBitmap(item.getItemType().getImage(),
                item.getHitbox().left + cameraX,
                item.getHitbox().top + cameraY,
                null);
    }

    public Doorway isPlayerOnDoorway(RectF playerHitbox) {
        for (Doorway doorway : currentMap.getDoorwayArrayList())
            if (doorway.isPlayerInsideDoorway(playerHitbox, cameraX, cameraY))
                return doorway;

        return null;
    }

    public void changeMap(Doorway doorwayTarget) {
        this.currentMap = doorwayTarget.getGameMapLocatedIn();

        float cX = MainActivity.GAME_WIDTH / 2f - doorwayTarget.getPosOfDoorway().x + GameConstants.Sprite.HITBOX_SIZE / 2f;
        float cY = MainActivity.GAME_HEIGHT / 2f - doorwayTarget.getPosOfDoorway().y + GameConstants.Sprite.HITBOX_SIZE / 2f;

        playing.setCameraValues(new PointF(cX, cY));
        cameraX = cX;
        cameraY = cY;

        playing.setDoorwayJustPassed(true);
    }

    public GameMap getCurrentMap() {
        return currentMap;
    }

    public void resetMapToInitialState() {
        // Clear tất cả enemies hiện có trên map
        if (currentMap.getSkeletonArrayList() != null) {
            currentMap.getSkeletonArrayList().clear();
        }
        if (currentMap.getMonsterArrayList() != null) {
            currentMap.getMonsterArrayList().clear();
        }
        if (currentMap.getBoomArrayList() != null) {
            currentMap.getBoomArrayList().clear();
        }
        if (currentMap.getItemArrayList() != null) {
            currentMap.getItemArrayList().clear();
        }

        // Khôi phục lại số lượng quái vật BAN ĐẦU như khi vào game lần đầu
        // Tạo lại 5 Skeleton ngẫu nhiên (như trong initTestMap)
        ArrayList<Skeleton> initialSkeletons = HelpMethods.GetSkeletonsRandomized(5, getCurrentMapArray());
        if (currentMap.getSkeletonArrayList() != null) {
            currentMap.getSkeletonArrayList().addAll(initialSkeletons);
        }

        // Tạo lại 3 Boom ngẫu nhiên (như trong initTestMap)
        ArrayList<Boom> initialBooms = HelpMethods.GetBoomsRandomized(3, getCurrentMapArray());
        if (currentMap.getBoomArrayList() != null) {
            currentMap.getBoomArrayList().addAll(initialBooms);
            // Set playing reference cho các Boom mới
            for (Boom boom : initialBooms) {
                boom.setPlaying(playing);
            }
        }

        // Monster ban đầu = 0 (như trong initTestMap), nên không cần tạo

        // Khôi phục lại items ban đầu
        ArrayList<Item> initialItems = new ArrayList<>();
        initialItems.add(new Item(Items.FISH, new PointF(560, 560)));
        initialItems.add(new Item(Items.MEDIPACK, new PointF(200, 700)));
        initialItems.add(new Item(Items.EMPTY_POT, new PointF(300, 150)));

        if (currentMap.getItemArrayList() != null) {
            currentMap.getItemArrayList().addAll(initialItems);
        }

        System.out.println("🔄 Map đã được reset về trạng thái ban đầu:");
        System.out.println("👹 5 Skeletons được tạo lại");
        System.out.println("💥 3 Booms được tạo lại");
        System.out.println("🎁 3 Items ban đầu được tạo lại");
        System.out.println("🚫 0 Monsters (như ban đầu)");
    }

    private int[][] getCurrentMapArray() {
        // Trả về mảng map hiện tại để spawn enemies
        // Giả sử đây là outside map array (có thể cần điều chỉnh)
        return new int[][]{
                {188, 189, 279, 275, 187, 189, 279, 275, 279, 276, 275, 279, 275, 275, 279, 275, 278, 276, 275, 278, 275, 279, 275},
                {188, 189, 275, 279, 187, 189, 276, 275, 279, 275, 277, 275, 275, 277, 276, 275, 279, 278, 278, 275, 275, 279, 275},
                {188, 189, 275, 276, 187, 189, 276, 279, 275, 278, 279, 279, 275, 275, 278, 278, 275, 275, 275, 276, 275, 279, 275},
                {254, 189, 275, 279, 187, 214, 166, 166, 166, 166, 166, 166, 166, 167, 275, 276, 275, 276, 279, 277, 275, 279, 275},
                {188, 189, 275, 275, 209, 210, 210, 210, 210, 195, 210, 210, 193, 189, 275, 277, 168, 275, 278, 275, 275, 276, 275},
                {188, 189, 279, 276, 279, 275, 276, 275, 277, 190, 275, 279, 187, 189, 275, 279, 190, 275, 279, 275, 275, 279, 275},
                {188, 189, 275, 275, 275, 279, 278, 275, 275, 190, 276, 277, 187, 258, 232, 232, 239, 232, 232, 232, 232, 233, 275},
                {188, 189, 275, 279, 275, 275, 231, 232, 232, 238, 275, 275, 187, 189, 275, 275, 275, 275, 275, 275, 275, 275, 275},
                {188, 189, 276, 279, 278, 275, 276, 275, 275, 275, 275, 276, 187, 189, 276, 275, 277, 275, 279, 275, 279, 275, 276},
                {188, 189, 275, 275, 279, 275, 279, 275, 276, 275, 275, 277, 187, 189, 279, 275, 275, 275, 275, 275, 275, 275, 275},
                {188, 214, 167, 276, 275, 277, 275, 275, 278, 275, 276, 275, 187, 189, 275, 275, 278, 275, 275, 276, 275, 277, 275},
                {254, 188, 214, 167, 275, 278, 275, 275, 275, 275, 279, 275, 187, 189, 275, 275, 275, 168, 275, 275, 275, 275, 278},
                {188, 188, 188, 214, 167, 279, 275, 277, 275, 277, 276, 275, 187, 258, 232, 232, 232, 238, 275, 279, 275, 275, 279},
                {188, 188, 188, 253, 214, 167, 275, 277, 168, 275, 275, 275, 187, 189, 275, 275, 275, 275, 275, 279, 275, 275, 275},
                {253, 188, 188, 188, 256, 214, 167, 275, 235, 232, 232, 232, 259, 189, 279, 275, 275, 277, 275, 275, 275, 279, 275},
                {188, 188, 188, 254, 188, 256, 214, 167, 275, 275, 277, 275, 187, 189, 275, 278, 275, 275, 279, 275, 279, 278, 275}
        };
    }

    private void initTestMap() {

        int[][] outsideArray = {
                {188, 189, 279, 275, 187, 189, 279, 275, 279, 276, 275, 279, 275, 275, 279, 275, 278, 276, 275, 278, 275, 279, 275},
                {188, 189, 275, 279, 187, 189, 276, 275, 279, 275, 277, 275, 275, 277, 276, 275, 279, 278, 278, 275, 275, 279, 275},
                {188, 189, 275, 276, 187, 189, 276, 279, 275, 278, 279, 279, 275, 275, 278, 278, 275, 275, 275, 276, 275, 279, 275},
                {254, 189, 275, 279, 187, 214, 166, 166, 166, 166, 166, 166, 166, 167, 275, 276, 275, 276, 279, 277, 275, 279, 275},
                {188, 189, 275, 275, 209, 210, 210, 210, 210, 195, 210, 210, 193, 189, 275, 277, 168, 275, 278, 275, 275, 276, 275},
                {188, 189, 279, 276, 279, 275, 276, 275, 277, 190, 275, 279, 187, 189, 275, 279, 190, 275, 279, 275, 275, 279, 275},
                {188, 189, 275, 275, 275, 279, 278, 275, 275, 190, 276, 277, 187, 258, 232, 232, 239, 232, 232, 232, 232, 233, 275},
                {188, 189, 275, 279, 275, 275, 231, 232, 232, 238, 275, 275, 187, 189, 275, 275, 275, 275, 275, 275, 275, 275, 275},
                {188, 189, 276, 279, 278, 275, 276, 275, 275, 275, 275, 276, 187, 189, 276, 275, 277, 275, 279, 275, 279, 275, 276},
                {188, 189, 275, 275, 279, 275, 279, 275, 276, 275, 275, 277, 187, 189, 279, 275, 275, 275, 275, 275, 275, 275, 275},
                {188, 214, 167, 276, 275, 277, 275, 275, 278, 275, 276, 275, 187, 189, 275, 275, 278, 275, 275, 276, 275, 277, 275},
                {254, 188, 214, 167, 275, 278, 275, 275, 275, 275, 279, 275, 187, 189, 275, 275, 275, 168, 275, 275, 275, 275, 278},
                {188, 188, 188, 214, 167, 279, 275, 277, 275, 277, 276, 275, 187, 258, 232, 232, 232, 238, 275, 279, 275, 275, 279},
                {188, 188, 188, 253, 214, 167, 275, 277, 168, 275, 275, 275, 187, 189, 275, 275, 275, 275, 275, 279, 275, 275, 275},
                {253, 188, 188, 188, 256, 214, 167, 275, 235, 232, 232, 232, 259, 189, 279, 275, 275, 277, 275, 275, 275, 279, 275},
                {188, 188, 188, 254, 188, 256, 214, 167, 275, 275, 277, 275, 187, 189, 275, 278, 275, 275, 279, 275, 279, 278, 275}
        };

        int[][] insideArray = {
                {374, 377, 377, 377, 377, 377, 378},
                {396, 0, 1, 1, 1, 2, 400},
                {396, 22, 23, 23, 23, 24, 400},
                {396, 22, 23, 23, 23, 24, 400},
                {396, 22, 23, 23, 23, 24, 400},
                {396, 44, 45, 45, 45, 46, 400},
                {462, 465, 463, 394, 464, 465, 466}
        };

        int[][] insideFlatHouseArray = {
                {389, 392, 392, 392, 392, 392, 393},
                {411, 143, 144, 144, 144, 145, 415},
                {411, 165, 166, 166, 166, 167, 415},
                {411, 165, 166, 166, 166, 167, 415},
                {411, 165, 166, 166, 166, 167, 415},
                {411, 187, 188, 188, 188, 189, 415},
                {477, 480, 478, 394, 479, 480, 481}
        };

        int[][] insideGreenRoofHouseArr = {
                {384, 387, 387, 387, 387, 387, 388},
                {406, 298, 298, 298, 298, 298, 410},
                {406, 298, 298, 298, 298, 298, 410},
                {406, 298, 298, 298, 298, 298, 410},
                {406, 298, 298, 298, 298, 298, 410},
                {406, 298, 298, 298, 298, 298, 410},
                {472, 475, 473, 394, 474, 475, 476}
        };
        // Khởi tạo buildings
        ArrayList<Building> buildingArrayList = new ArrayList<>();
        buildingArrayList.add(new Building(new PointF(1440, 160), Buildings.HOUSE_ONE));
        buildingArrayList.add(new Building(new PointF(1540, 880), Buildings.HOUSE_TWO));
        buildingArrayList.add(new Building(new PointF(575, 1000), Buildings.HOUSE_SIX));

        // Khởi tạo game objects
        ArrayList<GameObject> gameObjectArrayList = new ArrayList<>();
        gameObjectArrayList.add(new GameObject(new PointF(190, 70), GameObjects.STATUE_ANGRY_YELLOW));
        gameObjectArrayList.add(new GameObject(new PointF(580, 70), GameObjects.STATUE_ANGRY_YELLOW));
        gameObjectArrayList.add(new GameObject(new PointF(1000, 550), GameObjects.BASKET_FULL_RED_FRUIT));
        gameObjectArrayList.add(new GameObject(new PointF(620, 520), GameObjects.OVEN_SNOW_YELLOW));

        // Khởi tạo items ngoài trời
        ArrayList<Item> outsideItemArrayList = new ArrayList<>();
        outsideItemArrayList.add(new Item(Items.FISH, new PointF(560, 560)));
        outsideItemArrayList.add(new Item(Items.MEDIPACK, new PointF(200, 700)));
        outsideItemArrayList.add(new Item(Items.EMPTY_POT, new PointF(300, 150)));

        // Khởi tạo quái Skeleton, Monster và Boom
// spawn skeletons, monsters & booms
        ArrayList<Skeleton> skeletonsOutside = HelpMethods.GetSkeletonsRandomized(5, outsideArray);
        ArrayList<Monster> monstersOutside = HelpMethods.GetMonstersRandomized(0, outsideArray);
        ArrayList<Boom> boomsOutside = HelpMethods.GetBoomsRandomized(3, outsideArray);

// inside maps (skeletons only)
        GameMap insideMap = new GameMap(
                insideArray,
                Tiles.INSIDE,
                null,
                null,
                HelpMethods.GetSkeletonsRandomized(2, insideArray), // skeletons
                null, // monsters
                null, // booms
                null  // items
        );

        GameMap insideFlatRoofHouseMap = new GameMap(
                insideFlatHouseArray,
                Tiles.INSIDE,
                null,
                null,
                null, // skeletons
                null, // monsters
                null, // booms
                null  // items
        );

        GameMap insideGreenRoofHouseMap = new GameMap(
                insideGreenRoofHouseArr,
                Tiles.INSIDE,
                null,
                null,
                null, // skeletons
                null, // monsters
                null, // booms
                null  // items
        );

// outside map: buildings, objects, skeletons, monsters, booms, items
        GameMap outsideMap = new GameMap(
                outsideArray,
                Tiles.OUTSIDE,
                buildingArrayList,
                gameObjectArrayList,
                skeletonsOutside,
                monstersOutside,
                boomsOutside,
                outsideItemArrayList
        );

        // Nối các doorway
        HelpMethods.ConnectTwoDoorways(outsideMap,
                HelpMethods.CreatePointForDoorway(outsideMap, 0),
                insideMap,
                HelpMethods.CreatePointForDoorway(3, 6));

        HelpMethods.ConnectTwoDoorways(outsideMap,
                HelpMethods.CreatePointForDoorway(outsideMap, 1),
                insideFlatRoofHouseMap,
                HelpMethods.CreatePointForDoorway(3, 6));

        HelpMethods.ConnectTwoDoorways(outsideMap,
                HelpMethods.CreatePointForDoorway(outsideMap, 2),
                insideGreenRoofHouseMap,
                HelpMethods.CreatePointForDoorway(3, 6));

        currentMap = outsideMap;
    }
}
