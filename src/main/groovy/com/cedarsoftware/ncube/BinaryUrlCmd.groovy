package com.cedarsoftware.ncube

import com.cedarsoftware.util.UrlUtilities
import groovy.transform.CompileStatic
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

/**
 * Process a binary type (byte[]) that is specified at a URL.
 *
 * @author John DeRegnaucourt (jdereg@gmail.com)
 *         <br>
 *         Copyright (c) Cedar Software LLC
 *         <br><br>
 *         Licensed under the Apache License, Version 2.0 (the "License")
 *         you may not use this file except in compliance with the License.
 *         You may obtain a copy of the License at
 *         <br><br>
 *         http://www.apache.org/licenses/LICENSE-2.0
 *         <br><br>
 *         Unless required by applicable law or agreed to in writing, software
 *         distributed under the License is distributed on an "AS IS" BASIS,
 *         WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *         See the License for the specific language governing permissions and
 *         limitations under the License.
 */
@CompileStatic
public class BinaryUrlCmd extends ContentCmdCell
{
    private static final Logger LOG = LogManager.getLogger(BinaryUrlCmd.class)
    //  Private constructor only for serialization.
    private BinaryUrlCmd() {}

    public BinaryUrlCmd(String url, boolean cache)
    {
        super(null, url, cache)
    }

    protected Object simpleFetch(Map ctx)
    {
        URL u
        NCube cube = getNCube(ctx)

        for (int i=0; i < 2; i++)
        {
            try
            {
                u = getActualUrl(ctx)
            }
            catch (Exception e)
            {   // Do not retry (mark as BAD)
                if (i == 1)
                {   // Note: Error is marked, it will not be retried in the future
                    setErrorMessage("Invalid URL in byte[] cell (malformed or cannot resolve given classpath): " + getUrl() + ", cube: " + cube.getName() + ", version: " + cube.getVersion())
                    LOG.warn('BinaryUrlCmd: failed 2nd attempt [will NOT retry in future] getActualUrl() - unable to resolve against sys.classpath, url: ' + getUrl() + ", cube: " + cube.getName())
                    throw new IllegalStateException(getErrorMessage(), e)
                }
                else
                {
                    LOG.warn('BinaryUrlCmd: retrying getActualUrl() - unable to resolve against sys.classpath, url: ' + getUrl() + ", cube: " + cube.getName())
                    Thread.sleep(100)
                }
            }
        }

        // Try to load twice.
        for (int i=0; i < 2; i++)
        {
            try
            {
                return UrlUtilities.getContentFromUrl(u, true)
            }
            catch (Exception e)
            {
                if (i == 1)
                {   // Note: Error is not marked - it will be retried in the future
                    String msg = "Unable to load binary content from URL: " + getUrl() + ", cube: " + cube.getName() + ", version: " + cube.getVersion()
                    LOG.warn('BinaryUrlCmd: failed 2nd attempt [will retry in future] UrlUtilities.getContentFromUrl() - unable to fetch full contents, url: ' + getUrl() + ", cube: " + cube.getName())
                    throw new IllegalStateException(msg, e)
                }
                else
                {
                    LOG.warn('BinaryUrlCmd: retrying UrlUtilities.getContentFromUrl() - unable to fetch full contents, url: ' + getUrl() + ", cube: " + cube.getName())
                    Thread.sleep(100)
                }
            }
        }
    }
}