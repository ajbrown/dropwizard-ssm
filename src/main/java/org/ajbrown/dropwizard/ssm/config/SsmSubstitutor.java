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

import javax.annotation.Nonnull;

import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder;
import io.dropwizard.configuration.UndefinedEnvironmentVariableException;
import org.apache.commons.text.StringSubstitutor;

/**
 * Uses Amazon Simple Systems manager parameter stores to substitute configuration values.
 */
public class SsmSubstitutor
    extends StringSubstitutor
{
  public SsmSubstitutor(final boolean strict) {
    this(AWSSimpleSystemsManagementClientBuilder.defaultClient(), strict, false, null);
  }

  public SsmSubstitutor(final boolean strict, final String namespacePrefix) {
    this(AWSSimpleSystemsManagementClientBuilder.defaultClient(), strict, false, namespacePrefix);
  }


  public SsmSubstitutor(@Nonnull final AWSSimpleSystemsManagement ssm, boolean strict, final String namespacePrefix) {
    this(ssm, strict, false, namespacePrefix);
  }

  /**
   * Constructor
   *
   * @param ssm                     Amazon SSM client
   * @param strict                  {@code true} if looking up undefined environment variables
   *                                should throw a {@link UndefinedEnvironmentVariableException},
   *                                {@code false} otherwise.
   * @param substitutionInVariables a flag whether substitution is done in variable names.
   * @param namespacePrefix         A prefix to prepend to variable names when determining the parameter key.
   *                                <p>
   *                                Example: if this is set to "foobar/" and you look up the variable "buzz/bar",  this
   *                                SsmLookup will look up the value in a parameter named "foobar/buzz/bar".
   *                                </p>
   *
   *                                <p>This is mostly useful for re-using a variable name across multiple environments</p>
   * @see io.dropwizard.configuration.EnvironmentVariableLookup
   * @see org.apache.commons.text.StringSubstitutor#setEnableSubstitutionInVariables(boolean)
   */
  public SsmSubstitutor(@Nonnull final AWSSimpleSystemsManagement ssm, boolean strict,
                        boolean substitutionInVariables, final String namespacePrefix)
  {
    super(new SsmLookup(ssm, strict, namespacePrefix));
    this.setEnableSubstitutionInVariables(substitutionInVariables);
  }
}
