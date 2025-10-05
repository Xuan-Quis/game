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
    private GameMap map1; // Original outdoor map
    private GameMap map2; // Snow map
    private float cameraX, cameraY;
    private Playing playing;
    private int currentMapLevel = 1; // Track which map we're on (1 or 2)

    public MapManager(Playing playing) {
        this.playing = playing;
        initMaps();
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

    public int getCurrentMapLevel() {
        return currentMapLevel;
    }

    public void progressToNextMap() {
        if (currentMapLevel == 1) {
            // Move to snow map (map 2)
            currentMapLevel = 2;
            currentMap = map2;
            System.out.println("🏔️ Chuyển sang Map 2 - Snow World!");

            // Reset camera to center of new map
            float cX = MainActivity.GAME_WIDTH / 2f - (currentMap.getMapWidth() / 2f);
            float cY = MainActivity.GAME_HEIGHT / 2f - (currentMap.getMapHeight() / 2f);
            playing.setCameraValues(new PointF(cX, cY));
            cameraX = cX;
            cameraY = cY;
        }
    }

    // Method to reset map progression back to Map 1
    public void resetToMap1() {
        currentMapLevel = 1;
        currentMap = map1;
        System.out.println("🗺️ Reset về Map 1");
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

    private void initMaps() {
        // Map 1 - Original outdoor map
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

        // Map 2 - Snow-themed map with VALID tile indices (reusing existing tiles with slight variations)
        int[][] snowArray = {
                {420, 421, 419, 422, 419, 420, 422, 421, 419, 420, 421, 422, 419, 420, 421, 422, 419, 420, 421, 422, 419, 420, 421},
                {421, 422, 419, 420, 421, 422, 419, 420, 421, 422, 419, 420, 421, 422, 419, 420, 421, 422, 419, 420, 421, 422, 419},
                {419, 420, 421, 422, 419, 420, 421, 422, 419, 420, 421, 422, 419, 420, 421, 422, 419, 420, 421, 422, 419, 420, 421},
                {422, 419, 308, 309, 310, 419, 420, 421, 422, 419, 420, 421, 422, 419, 420, 421, 422, 419, 420, 421, 422, 419, 420},
                {420, 421, 330, 331, 332, 421, 422, 419, 420, 421, 422, 419, 420, 421, 422, 419, 420, 421, 422, 419, 420, 421, 422},
                {419, 420, 352, 353, 354, 420, 421, 422, 419, 420, 421, 422, 419, 420, 421, 422, 419, 420, 421, 422, 419, 420, 421},
                {422, 419, 420, 421, 422, 419, 420, 421, 422, 419, 420, 421, 422, 419, 420, 421, 422, 419, 420, 421, 422, 419, 420},
                {420, 421, 422, 419, 420, 421, 422, 419, 420, 421, 422, 419, 420, 421, 422, 419, 420, 421, 422, 419, 420, 421, 422},
                {419, 420, 421, 422, 419, 420, 421, 422, 419, 420, 421, 422, 419, 420, 421, 422, 419, 420, 421, 422, 419, 420, 421},
                {422, 419, 420, 421, 422, 419, 420, 421, 422, 419, 420, 421, 422, 419, 420, 421, 422, 419, 420, 421, 422, 419, 420},
                {420, 421, 422, 419, 420, 421, 422, 419, 420, 421, 422, 419, 420, 421, 422, 419, 420, 421, 422, 419, 420, 421, 422},
                {419, 420, 421, 422, 419, 420, 421, 422, 419, 420, 421, 422, 419, 420, 421, 422, 419, 420, 421, 422, 419, 420, 421},
                {422, 419, 420, 421, 422, 419, 420, 421, 422, 419, 420, 421, 422, 419, 420, 421, 422, 419, 420, 421, 422, 419, 420},
                {420, 421, 422, 419, 420, 421, 422, 419, 420, 421, 422, 419, 420, 421, 422, 419, 420, 421, 422, 419, 420, 421, 422},
                {419, 420, 421, 422, 419, 420, 421, 422, 419, 420, 421, 422, 419, 420, 421, 422, 419, 420, 421, 422, 419, 420, 421},
                {422, 419, 420, 421, 422, 419, 420, 421, 422, 419, 420, 421, 422, 419, 420, 421, 422, 419, 420, 421, 422, 419, 420}
        };

        // Buildings for Map 1
        ArrayList<Building> buildingArrayList1 = new ArrayList<>();
        buildingArrayList1.add(new Building(new PointF(1440, 160), Buildings.HOUSE_ONE));
        buildingArrayList1.add(new Building(new PointF(1540, 880), Buildings.HOUSE_TWO));
        buildingArrayList1.add(new Building(new PointF(575, 1000), Buildings.HOUSE_SIX));

        // Buildings for Map 2 (Snow map) - different positions
        ArrayList<Building> buildingArrayList2 = new ArrayList<>();
        buildingArrayList2.add(new Building(new PointF(800, 200), Buildings.HOUSE_ONE));
        buildingArrayList2.add(new Building(new PointF(400, 600), Buildings.HOUSE_TWO));
        buildingArrayList2.add(new Building(new PointF(1200, 800), Buildings.HOUSE_SIX));

        // Game objects for Map 1
        ArrayList<GameObject> gameObjectArrayList1 = new ArrayList<>();
        gameObjectArrayList1.add(new GameObject(new PointF(190, 70), GameObjects.STATUE_ANGRY_YELLOW));
        gameObjectArrayList1.add(new GameObject(new PointF(580, 70), GameObjects.STATUE_ANGRY_YELLOW));
        gameObjectArrayList1.add(new GameObject(new PointF(1000, 550), GameObjects.BASKET_FULL_RED_FRUIT));
        gameObjectArrayList1.add(new GameObject(new PointF(620, 520), GameObjects.OVEN_SNOW_YELLOW));

        // Game objects for Map 2 (Snow map) - different positions and more snow-themed
        ArrayList<GameObject> gameObjectArrayList2 = new ArrayList<>();
        gameObjectArrayList2.add(new GameObject(new PointF(300, 100), GameObjects.OVEN_SNOW_YELLOW));
        gameObjectArrayList2.add(new GameObject(new PointF(700, 300), GameObjects.OVEN_SNOW_YELLOW));
        gameObjectArrayList2.add(new GameObject(new PointF(1100, 500), GameObjects.BASKET_FULL_RED_FRUIT));
        gameObjectArrayList2.add(new GameObject(new PointF(500, 700), GameObjects.STATUE_ANGRY_YELLOW));

        // Items for Map 1
        ArrayList<Item> outsideItemArrayList1 = new ArrayList<>();
        outsideItemArrayList1.add(new Item(Items.FISH, new PointF(560, 560)));
        outsideItemArrayList1.add(new Item(Items.MEDIPACK, new PointF(200, 700)));
        outsideItemArrayList1.add(new Item(Items.EMPTY_POT, new PointF(300, 150)));

        // Items for Map 2 (Snow map) - different positions
        ArrayList<Item> outsideItemArrayList2 = new ArrayList<>();
        outsideItemArrayList2.add(new Item(Items.MEDIPACK, new PointF(400, 400)));
        outsideItemArrayList2.add(new Item(Items.FISH, new PointF(800, 600)));
        outsideItemArrayList2.add(new Item(Items.EMPTY_POT, new PointF(600, 200)));
        outsideItemArrayList2.add(new Item(Items.MEDIPACK, new PointF(1000, 700))); // Extra medipack for harder map

        // Enemies for Map 1 (easier)
        ArrayList<Skeleton> skeletonsOutside1 = HelpMethods.GetSkeletonsRandomized(5, outsideArray);
        ArrayList<Monster> monstersOutside1 = HelpMethods.GetMonstersRandomized(0, outsideArray);
        ArrayList<Boom> boomsOutside1 = HelpMethods.GetBoomsRandomized(3, outsideArray);

        // Enemies for Map 2 (harder - more enemies)
        ArrayList<Skeleton> skeletonsOutside2 = HelpMethods.GetSkeletonsRandomized(8, snowArray); // More skeletons
        ArrayList<Monster> monstersOutside2 = HelpMethods.GetMonstersRandomized(3, snowArray); // Add monsters
        ArrayList<Boom> boomsOutside2 = HelpMethods.GetBoomsRandomized(5, snowArray); // More booms

        // Set playing reference for booms in both maps
        for (Boom boom : boomsOutside1) {
            boom.setPlaying(playing);
        }
        for (Boom boom : boomsOutside2) {
            boom.setPlaying(playing);
        }

        // Create Map 1 (Original outdoor map)
        map1 = new GameMap(
                outsideArray,
                Tiles.OUTSIDE,
                buildingArrayList1,
                gameObjectArrayList1,
                skeletonsOutside1,
                monstersOutside1,
                boomsOutside1,
                outsideItemArrayList1
        );

        // Create Map 2 (Snow map)
        map2 = new GameMap(
                snowArray,
                Tiles.SNOW,
                buildingArrayList2,
                gameObjectArrayList2,
                skeletonsOutside2,
                monstersOutside2,
                boomsOutside2,
                outsideItemArrayList2
        );

        // Set current map to map1
        currentMap = map1;

        // Create inside maps for map1 (keeping existing functionality)
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

        // Inside maps (only for map1)
        GameMap insideMap = new GameMap(
                insideArray,
                Tiles.INSIDE,
                null,
                null,
                HelpMethods.GetSkeletonsRandomized(2, insideArray),
                null,
                null,
                null
        );

        GameMap insideFlatRoofHouseMap = new GameMap(
                insideFlatHouseArray,
                Tiles.INSIDE,
                null,
                null,
                null,
                null,
                null,
                null
        );

        GameMap insideGreenRoofHouseMap = new GameMap(
                insideGreenRoofHouseArr,
                Tiles.INSIDE,
                null,
                null,
                null,
                null,
                null,
                null
        );

        // Connect doorways for map1
        HelpMethods.ConnectTwoDoorways(map1,
                HelpMethods.CreatePointForDoorway(map1, 0),
                insideMap,
                HelpMethods.CreatePointForDoorway(3, 6));

        HelpMethods.ConnectTwoDoorways(map1,
                HelpMethods.CreatePointForDoorway(map1, 1),
                insideFlatRoofHouseMap,
                HelpMethods.CreatePointForDoorway(3, 6));

        HelpMethods.ConnectTwoDoorways(map1,
                HelpMethods.CreatePointForDoorway(map1, 2),
                insideGreenRoofHouseMap,
                HelpMethods.CreatePointForDoorway(3, 6));
    }
}
