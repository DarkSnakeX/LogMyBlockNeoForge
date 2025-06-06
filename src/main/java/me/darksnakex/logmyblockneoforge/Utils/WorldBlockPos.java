package me.darksnakex.blocktracker.Utils;

import net.minecraft.core.BlockPos;

public record WorldBlockPos(String worldName, BlockPos blockPos) {

    public static WorldBlockPos fromString(String str) {
        try {
            String[] parts = str.split(", Pos: ");
            String worldName = parts[0].replace("World: ", "").trim();

            String posPart = parts[1].replace("BlockPos{", "").replace("}", "").trim();
            String[] coords = posPart.split(", ");

            int x = Integer.parseInt(coords[0].split("=")[1]);
            int y = Integer.parseInt(coords[1].split("=")[1]);
            int z = Integer.parseInt(coords[2].split("=")[1]);

            BlockPos blockPos = new BlockPos(x, y, z);
            return new WorldBlockPos(worldName, blockPos);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid WorldBlockPos format: " + str, e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorldBlockPos that = (WorldBlockPos) o;
        return worldName.equals(that.worldName) && blockPos.equals(that.blockPos);
    }

    @Override
    public String toString() {
        return "World: " + worldName + ", Pos: " + blockPos;
    }
}


