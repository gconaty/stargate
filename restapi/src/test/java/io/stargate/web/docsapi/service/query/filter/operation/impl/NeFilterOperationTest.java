/*
 * Copyright The Stargate Authors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.stargate.web.docsapi.service.query.filter.operation.impl;

import static org.assertj.core.api.Assertions.assertThat;

import io.stargate.db.query.Predicate;
import io.stargate.web.docsapi.service.query.filter.operation.FilterOperationCode;
import java.util.Optional;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class NeFilterOperationTest {

  NeFilterOperation ne = NeFilterOperation.of();

  @Nested
  class FilterTest {

    @Test
    public void stringEquals() {
      boolean result = ne.test("filterValue", "filterValue");

      assertThat(result).isFalse();
    }

    @Test
    public void stringNotEquals() {
      boolean result = ne.test("filterValue", "dbValue");

      assertThat(result).isTrue();
    }

    @Test
    public void stringNotEqualsNull() {
      boolean result = ne.test("filterValue", null);

      assertThat(result).isTrue();
    }

    @Test
    public void booleanEquals() {
      boolean value = RandomUtils.nextBoolean();

      boolean result = ne.test(value, value);

      assertThat(result).isFalse();
    }

    @Test
    public void booleanNotEquals() {
      boolean value = RandomUtils.nextBoolean();

      boolean result = ne.test(value, !value);

      assertThat(result).isTrue();
    }

    @Test
    public void booleanNotEqualsNull() {
      boolean value = RandomUtils.nextBoolean();

      boolean result = ne.test(value, null);

      assertThat(result).isTrue();
    }

    @Test
    public void numberEquals() {
      boolean result = ne.test(22d, 22d);

      assertThat(result).isFalse();
    }

    @Test
    public void numberEqualsDifferentTypes() {
      boolean result = ne.test(22, 22d);

      assertThat(result).isFalse();
    }

    @Test
    public void numbersNotEquals() {
      boolean result = ne.test(22d, 23d);

      assertThat(result).isTrue();
    }

    @Test
    public void numbersNotEqualsDifferentTypes() {
      boolean result = ne.test(22L, 22.01);

      assertThat(result).isTrue();
    }

    @Test
    public void numbersNotEqualsNull() {
      boolean result = ne.test(22, null);

      assertThat(result).isTrue();
    }
  }

  @Nested
  class GetDatabasePredicate {

    @Test
    public void correct() {
      Optional<Predicate> result = ne.getQueryPredicate();

      assertThat(result).isEmpty();
    }
  }

  @Nested
  class GetOpCode {

    @Test
    public void correct() {
      FilterOperationCode result = ne.getOpCode();

      assertThat(result).isEqualTo(FilterOperationCode.NE);
    }
  }

  @Nested
  class IsEvaluateOnMissingFields {

    @Test
    public void evaluate() {
      boolean result = ne.isEvaluateOnMissingFields();

      assertThat(result).isTrue();
    }
  }
}