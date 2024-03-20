package org.apache.fop.complexscripts.util;

import java.util.Arrays;

public final class CharMirror {
   private static int[] mirroredCharacters = new int[]{40, 41, 60, 62, 91, 93, 123, 125, 171, 187, 3898, 3899, 3900, 3901, 5787, 5788, 8249, 8250, 8261, 8262, 8317, 8318, 8333, 8334, 8712, 8713, 8714, 8715, 8716, 8717, 8725, 8764, 8765, 8771, 8786, 8787, 8788, 8789, 8804, 8805, 8806, 8807, 8808, 8809, 8810, 8811, 8814, 8815, 8816, 8817, 8818, 8819, 8820, 8821, 8822, 8823, 8824, 8825, 8826, 8827, 8828, 8829, 8830, 8831, 8832, 8833, 8834, 8835, 8836, 8837, 8838, 8839, 8840, 8841, 8842, 8843, 8847, 8848, 8849, 8850, 8856, 8866, 8867, 8870, 8872, 8873, 8875, 8880, 8881, 8882, 8883, 8884, 8885, 8886, 8887, 8905, 8906, 8907, 8908, 8909, 8912, 8913, 8918, 8919, 8920, 8921, 8922, 8923, 8924, 8925, 8926, 8927, 8928, 8929, 8930, 8931, 8932, 8933, 8934, 8935, 8936, 8937, 8938, 8939, 8940, 8941, 8944, 8945, 8946, 8947, 8948, 8950, 8951, 8954, 8955, 8956, 8957, 8958, 8968, 8969, 8970, 8971, 9001, 9002, 10088, 10089, 10090, 10091, 10092, 10093, 10094, 10095, 10096, 10097, 10098, 10099, 10100, 10101, 10179, 10180, 10181, 10182, 10184, 10185, 10197, 10198, 10205, 10206, 10210, 10211, 10212, 10213, 10214, 10215, 10216, 10217, 10218, 10219, 10220, 10221, 10222, 10223, 10627, 10628, 10629, 10630, 10631, 10632, 10633, 10634, 10635, 10636, 10637, 10638, 10639, 10640, 10641, 10642, 10643, 10644, 10645, 10646, 10647, 10648, 10680, 10688, 10689, 10692, 10693, 10703, 10704, 10705, 10706, 10708, 10709, 10712, 10713, 10714, 10715, 10741, 10744, 10745, 10748, 10749, 10795, 10796, 10797, 10798, 10804, 10805, 10812, 10813, 10852, 10853, 10873, 10874, 10877, 10878, 10879, 10880, 10881, 10882, 10883, 10884, 10891, 10892, 10897, 10898, 10899, 10900, 10901, 10902, 10903, 10904, 10905, 10906, 10907, 10908, 10913, 10914, 10918, 10919, 10920, 10921, 10922, 10923, 10924, 10925, 10927, 10928, 10931, 10932, 10947, 10948, 10949, 10950, 10957, 10958, 10959, 10960, 10961, 10962, 10963, 10964, 10965, 10966, 10974, 10979, 11778, 11779, 11780, 11781, 11785, 11786, 11788, 11789, 11804, 11805, 11808, 11809, 11810, 11811, 11812, 11813, 11814, 12302, 12303, 12304, 12305, 12308, 12309, 12310, 12311, 12312, 12313, 12314, 12315, 65113, 65114, 65339, 65341, 65371, 65373, 65375, 65376, 65378, 65379};
   private static int[] mirroredCharactersMapping = new int[]{41, 40, 62, 60, 93, 91, 125, 123, 187, 171, 3899, 3898, 3901, 3900, 5788, 5787, 8250, 8249, 8262, 8261, 8318, 8317, 8334, 8333, 8715, 8716, 8717, 8712, 8713, 8714, 10741, 8765, 8764, 8909, 8787, 8786, 8789, 8788, 8805, 8804, 8807, 8806, 8809, 8808, 8811, 8810, 8815, 8814, 8817, 8816, 8819, 8818, 8821, 8820, 8823, 8822, 8825, 8824, 8827, 8826, 8829, 8828, 8831, 8830, 8833, 8832, 8835, 8834, 8837, 8836, 8839, 8838, 8841, 8840, 8843, 8842, 8848, 8847, 8850, 8849, 10680, 8867, 8866, 10974, 10980, 10979, 10981, 8881, 8880, 8883, 8882, 8885, 8884, 8887, 8886, 8906, 8905, 8908, 8907, 8771, 8913, 8912, 8919, 8918, 8921, 8920, 8923, 8922, 8925, 8924, 8927, 8926, 8929, 8928, 8931, 8930, 8933, 8932, 8935, 8934, 8937, 8936, 8939, 8938, 8941, 8940, 8945, 8944, 8954, 8955, 8956, 8957, 8958, 8946, 8947, 8948, 8950, 8951, 8969, 8968, 8971, 8970, 9002, 9001, 10089, 10088, 10091, 10090, 10093, 10092, 10095, 10094, 10097, 10096, 10099, 10098, 10101, 10100, 10180, 10179, 10182, 10181, 10185, 10184, 10198, 10197, 10206, 10205, 10211, 10210, 10213, 10212, 10215, 10214, 10217, 10216, 10219, 10218, 10221, 10220, 10223, 10222, 10628, 10627, 10630, 10629, 10632, 10631, 10634, 10633, 10636, 10635, 10640, 10639, 10638, 10637, 10642, 10641, 10644, 10643, 10646, 10645, 10648, 10647, 8856, 10689, 10688, 10693, 10692, 10704, 10703, 10706, 10705, 10709, 10708, 10713, 10712, 10715, 10714, 8725, 10745, 10744, 10749, 10748, 10796, 10795, 10798, 10797, 10805, 10804, 10813, 10812, 10853, 10852, 10874, 10873, 10878, 10877, 10880, 10879, 10882, 10881, 10884, 10883, 10892, 10891, 10898, 10897, 10900, 10899, 10902, 10901, 10904, 10903, 10906, 10905, 10908, 10907, 10914, 10913, 10919, 10918, 10921, 10920, 10923, 10922, 10925, 10924, 10928, 10927, 10932, 10931, 10948, 10947, 10950, 10949, 10958, 10957, 10960, 10959, 10962, 10961, 10964, 10963, 10966, 10965, 8870, 8873, 11779, 11778, 11781, 11780, 11786, 11785, 11789, 11788, 11805, 11804, 11809, 11808, 11811, 11810, 11813, 11812, 11815, 12303, 12302, 12305, 12304, 12309, 12308, 12311, 12310, 12313, 12312, 12315, 12314, 65114, 65113, 65341, 65339, 65373, 65371, 65376, 65375, 65379, 65378};

   private CharMirror() {
   }

   public static String mirror(String s) {
      StringBuffer sb = new StringBuffer(s);
      int i = 0;

      for(int n = sb.length(); i < n; ++i) {
         sb.setCharAt(i, (char)mirror(sb.charAt(i)));
      }

      return sb.toString();
   }

   public static boolean hasMirrorable(String s) {
      int i = 0;

      for(int n = s.length(); i < n; ++i) {
         char c = s.charAt(i);
         if (Arrays.binarySearch(mirroredCharacters, c) >= 0) {
            return true;
         }
      }

      return false;
   }

   private static int mirror(int c) {
      int i = Arrays.binarySearch(mirroredCharacters, c);
      return i < 0 ? c : mirroredCharactersMapping[i];
   }
}
