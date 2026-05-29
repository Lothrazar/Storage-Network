package com.lothrazar.storagenetwork.api.batch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.lothrazar.storagenetwork.api.IConnectableLink;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class RequestBatch extends Batch<Request> {

  private static final long serialVersionUID = 9136459257621033386L;

  public void extractStacks(IConnectableLink providerStorage, Integer slot, Item item) {
    List<Request> requests = get(item);
    List<Request> remainingRequests = new ArrayList<Request>();
    for (Request request : requests) {
      if (!request.insertStack(providerStorage, slot)) {
        remainingRequests.add(request);
      }
      ItemStack stack = providerStorage.extractFromSlot(slot, 1, true);
      if (stack.isEmpty()) {
        return;
      }
    }
    put(item, remainingRequests);
  }

  public void sort() {
    Collection<List<Request>> requests = this.values();
    for (List<Request> requestList : requests) {
      quickSort(requestList, 0, requestList.size() - 1);
    }
  }

  private void quickSort(List<Request> requestList, int start, int end) {
    if (start < end) {
      int partitionInd = partition(requestList, start, end);
      quickSort(requestList, start, partitionInd - 1);
      quickSort(requestList, partitionInd + 1, end);
    }
  }

  private int partition(List<Request> requestList, int start, int end) {
    int pivot = requestList.get(end).getPriority();
    int i = (start - 1);
    for (int j = start; j < end; j++) {
      if (requestList.get(j).getPriority() <= pivot) {
        i++;
        Request highTemp = requestList.get(i);
        Request lowTemp = requestList.get(j);
        requestList.set(i, lowTemp);
        requestList.set(j, highTemp);
      }
    }
    Request swapTemp = requestList.get(i + 1);
    Request temp = requestList.get(end);
    requestList.set(i + 1, temp);
    requestList.set(end, swapTemp);
    return i + 1;
  }
}
