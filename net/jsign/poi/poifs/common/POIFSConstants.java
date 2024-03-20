package net.jsign.poi.poifs.common;

public interface POIFSConstants {
   POIFSBigBlockSize SMALLER_BIG_BLOCK_SIZE_DETAILS = new POIFSBigBlockSize(512, (short)9);
   POIFSBigBlockSize LARGER_BIG_BLOCK_SIZE_DETAILS = new POIFSBigBlockSize(4096, (short)12);
}
