package com.mason.finalpaper.utils;

import java.io.IOException;
import java.util.Arrays;

public class Practice {
  public static void main(String[] args) throws IOException {
    int[] data = {1, 2, 1, 1, 2, 1, 2, 3, 12, 1, 3, 12, 31, 23, 12, 3};
    int size = data.length;
    quickSort(data, 0, size - 1);
    Arrays.stream(data).forEach(System.out::println);
  }

  private static void quickSort(int[] data, int lo, int hi) {
    if (lo < hi) {
      int index = partition(data, lo, hi);
      quickSort(data, lo, index - 1);
      quickSort(data, index + 1, hi);
    }
  }

  private static int partition(int[] data, int lo, int hi) {
    int pivot = data[hi];
    int index = lo;
    for (int i = lo; i < hi; i++) {
      if (data[i] < pivot) {
        swap(data, i, index);
        index++;
      }
    }
    swap(data, index, hi);
    return index;
  }

  private static void swap(int[] data, int lo, int hi) {
    if (lo != hi) {
      int temp = data[lo];
      data[lo] = data[hi];
      data[hi] = temp;
    }
  }
}
