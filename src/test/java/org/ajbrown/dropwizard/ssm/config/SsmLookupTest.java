/*
 *  Copyright 2018 A.J. Brown
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ajbrown.dropwizard.ssm.config;

import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterRequest;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterResult;
import com.amazonaws.services.simplesystemsmanagement.model.Parameter;
import com.amazonaws.services.simplesystemsmanagement.model.ParameterNotFoundException;
import io.dropwizard.configuration.UndefinedEnvironmentVariableException;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;

/**
 * Unit test for {@link SsmLookup}
 */
final public class SsmLookupTest
{
  private final AWSSimpleSystemsManagement ssm = Mockito.mock(AWSSimpleSystemsManagement.class);

  @Test(expected = UndefinedEnvironmentVariableException.class)
  public void lookupMissingParameterWithStrictEnabled() throws Exception {
    SsmLookup ssmLookup = new SsmLookup(ssm, true);

    Mockito.when(ssm.getParameter(any(GetParameterRequest.class)))
        .thenThrow(new ParameterNotFoundException("not found"));

    ssmLookup.lookup("/foo/bar/key");
  }

  @Test
  public void lookupMissingParameterWithStrictDisabled() {
    SsmLookup ssmLookup = new SsmLookup(ssm, false);

    Mockito.when(ssm.getParameter(any(GetParameterRequest.class)))
        .thenThrow(new ParameterNotFoundException("not found"));

    String result = ssmLookup.lookup("/foo/bar/key");
    assertNull(result);
  }

  @Test
  public void lookupUsesSsmParameterWithSameName() {
    SsmLookup ssmLookup = new SsmLookup(ssm, false);

    ArgumentCaptor<GetParameterRequest> requestArgumentCaptor = ArgumentCaptor.forClass(GetParameterRequest.class);
    Mockito.when(ssm.getParameter(requestArgumentCaptor.capture()))
        .thenAnswer((Answer<GetParameterResult>) invocation -> {
          GetParameterRequest request = invocation.getArgumentAt(0, GetParameterRequest.class);
          return new GetParameterResult()
              .withParameter(new Parameter().withName(request.getName()).withValue("foo-value"));
        });

    String result = ssmLookup.lookup("/foo/bar/value");
    assertThat(result, is(equalTo("foo-value")));
    assertThat(requestArgumentCaptor.getValue().getName(), is(equalTo("/foo/bar/value")));
    assertTrue(requestArgumentCaptor.getValue().getWithDecryption());

  }

  @Test
  public void lookupParameterWithPrefix() {
    SsmLookup ssmLookup = new SsmLookup(ssm, false, "--myprefix--");

    ArgumentCaptor<GetParameterRequest> requestArgumentCaptor = ArgumentCaptor.forClass(GetParameterRequest.class);
    Mockito.when(ssm.getParameter(requestArgumentCaptor.capture()))
        .thenAnswer((Answer<GetParameterResult>) invocation -> {
          GetParameterRequest request = invocation.getArgumentAt(0, GetParameterRequest.class);
          return new GetParameterResult()
              .withParameter(new Parameter().withName(request.getName()).withValue("foo-value"));
        });

    String result = ssmLookup.lookup("/foo/bar/value");
    assertThat(result, is(equalTo("foo-value")));
    assertThat(requestArgumentCaptor.getValue().getName(), is(equalTo("--myprefix--/foo/bar/value")));
    assertTrue(requestArgumentCaptor.getValue().getWithDecryption());

  }
}
