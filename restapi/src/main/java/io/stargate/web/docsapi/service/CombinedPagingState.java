/*
 * Copyright The Stargate Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.stargate.web.docsapi.service;

import com.datastax.oss.driver.shaded.guava.common.collect.ImmutableList;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CombinedPagingState {

  private final List<ByteBuffer> nestedStates;

  private CombinedPagingState(List<ByteBuffer> nestedStates) {
    this.nestedStates = nestedStates;
  }

  public static CombinedPagingState of(List<ByteBuffer> nestedStates) {
    return new CombinedPagingState(nestedStates);
  }

  public ByteBuffer serialize() {
    if (nestedStates.stream().allMatch(Objects::isNull)) {
      return null;
    }

    if (nestedStates.size() == 1) {
      return nestedStates.get(0);
    }

    int toAllocate = 4; // size of int for element count
    for (ByteBuffer state : nestedStates) {
      toAllocate += 4; // size of int for element size
      toAllocate += state == null ? 0 : state.remaining();
    }

    ByteBuffer result = ByteBuffer.allocate(toAllocate);

    result.putInt(nestedStates.size());
    for (ByteBuffer state : nestedStates) {
      if (state != null) {
        ByteBuffer nested = state.slice();

        result.putInt(nested.remaining());
        result.put(nested);
      } else {
        result.putInt(-1); // no state
      }
    }

    result.flip();
    return result;
  }

  public static CombinedPagingState deserialize(int expectedSize, ByteBuffer data) {
    if (expectedSize <= 0) {
      throw new IllegalArgumentException("Invalid paging state size: " + expectedSize);
    }

    if (data == null) {
      ArrayList<ByteBuffer> buffers = new ArrayList<>(expectedSize);
      for (int i = 0; i < expectedSize; i++) {
        buffers.add(null);
      }

      return of(buffers);
    }

    if (expectedSize == 1) {
      return of(ImmutableList.of(data));
    }

    if (data.remaining() < 4) {
      throw new IllegalArgumentException(
          String.format(
              "Invalid paging state: unable to read size, available bytes: %d", data.remaining()));
    }

    int count = data.getInt();
    if (expectedSize != count) {
      throw new IllegalArgumentException(
          String.format(
              "Invalid paging state: expected element count: %d, actual: %d", expectedSize, count));
    }

    ArrayList<ByteBuffer> nested = new ArrayList<>(count);
    while (count-- > 0) {
      int size = data.getInt();

      ByteBuffer element;
      if (size >= 0) {
        element = data.slice();
        element.limit(size);
        data.position(data.position() + size);
      } else {
        element = null;
      }

      nested.add(element);
    }

    return of(nested);
  }

  public List<ByteBuffer> nested() {
    return nestedStates;
  }
}