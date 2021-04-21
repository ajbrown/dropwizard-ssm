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
import com.amazonaws.services.simplesystemsmanagement.model.ParameterNotFoundException;
import com.google.common.base.Strings;
import io.dropwizard.configuration.UndefinedEnvironmentVariableException;
import org.apache.commons.text.lookup.StringLookup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * StrLookup implementation which loads values from Amazon Simple Systems Management parameters.
 */
public class SsmLookup implements StringLookup
{
  private static final Logger log = LoggerFactory.getLogger(SsmLookup.class);

  private final AWSSimpleSystemsManagement ssm;

  private final boolean strict;

  private final String namespacePrefix;

  /**
   * Constructor.
   *
   * @param ssm    A configured AWSSimpleSystemsManagement client to use for parameter value requests.
   * @param strict {@code true} if lookups should throw an UndefinedEnvironmentVariableException when no value is found
   */
  public SsmLookup(final AWSSimpleSystemsManagement ssm, final boolean strict) {
    this(ssm, strict, null);
  }

  /**
   * Constructor.
   *
   * @param ssm             A configured AWSSimpleSystemsManagement client to use for parameter value requests.
   * @param strict          {@code true} if lookups should throw an UndefinedEnvironmentVariableException when no value is found
   * @param namespacePrefix A prefix to prepend to variable names when determining the parameter key.
   *                        <p>
   *                        Example: if this is set to "foobar/" and you look up the variable "buzz/bar",  this
   *                        SsmLookup will look up the value in a parameter named "foobar/buzz/bar".
   *                        </p>
   *
   *                        <p>This is mostly useful for re-using a variable name across multiple environments</p>
   */
  public SsmLookup(final AWSSimpleSystemsManagement ssm, final boolean strict, final String namespacePrefix) {
    this.ssm = ssm;
    this.strict = strict;
    this.namespacePrefix = namespacePrefix;
  }

  public String lookup(final String key) {
    final String paramName = Strings.isNullOrEmpty(namespacePrefix) ? key : String.format("%s%s", namespacePrefix, key);
    final GetParameterRequest request = new GetParameterRequest()
        .withName(paramName)
        .withWithDecryption(true);

    log.trace("Looking up value in ssm key '{}'", paramName);

    try {
      GetParameterResult result = ssm.getParameter(request);
      return result.getParameter().getValue();
    }
    catch (ParameterNotFoundException pnfe) {
      log.debug("Parameter not found in SSM: {}", paramName);

    }
    catch (Exception e) {
      log.warn(String.format("Error looking up parameter '%s' in SSM", paramName), e);
    }

    if (strict) {
      throw new UndefinedEnvironmentVariableException(String.format(
          "No parameter found in SSM with name '%s'.  Could not substitute expression '%s'"
          , paramName, key));
    }

    return null;
  }
}
