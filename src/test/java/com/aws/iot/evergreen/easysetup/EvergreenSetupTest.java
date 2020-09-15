package com.aws.iot.evergreen.easysetup;

import com.aws.iot.evergreen.kernel.Kernel;
import com.aws.iot.evergreen.testcommons.testutilities.EGExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.aws.iot.evergreen.testcommons.testutilities.ExceptionLogProtector.ignoreExceptionUltimateCauseWithMessage;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, EGExtension.class})
public class EvergreenSetupTest {
    @Mock
    private DeviceProvisioningHelper deviceProvisioningHelper;
    @Mock
    private Kernel kernel;

    @Mock
    private DeviceProvisioningHelper.ThingInfo thingInfo;

    private EvergreenSetup evergreenSetup;

    @Test
    void GIVEN_setup_script_WHEN_script_is_used_THEN_setup_actions_are_performed() throws Exception {
        when(deviceProvisioningHelper.createThing(any(), any(), any())).thenReturn(thingInfo);
        evergreenSetup =
                new EvergreenSetup(System.out, System.err, deviceProvisioningHelper, "--config", "mock_config_path", "--root",
                        "mock_root", "--thing-name", "mock_thing_name", "--policy-name", "mock_policy_name",
                        "--tes-role-name", "mock_tes_role_name", "--tes-role-alias-name", "mock_tes_role_alias_name",
                        "--provision", "y", "--setup-tes", "y", "--install-cli", "y", "--aws-region", "us-east-1",
                        "--tes-role-policy-name", "mock_policy_name", "--tes-role-policy-doc", "mock_policy_doc_json");
        evergreenSetup.parseArgs();
        evergreenSetup.provision(kernel);
        verify(deviceProvisioningHelper, times(1)).createThing(any(), any(), any());
        verify(deviceProvisioningHelper, times(1)).updateKernelConfigWithIotConfiguration(any(), any(), any());
        verify(deviceProvisioningHelper, times(1)).setupIoTRoleForTes(any(), any(), any());
        verify(deviceProvisioningHelper, times(1)).createAndAttachRolePolicy(any(), any(), any());
        verify(deviceProvisioningHelper, times(1)).updateKernelConfigWithTesRoleInfo(any(), any());
    }

    @Test
    void GIVEN_setup_script_WHEN_no_tes_policy_arguments_provided_THEN_tes_policy_no_op() throws Exception {
        when(deviceProvisioningHelper.createThing(any(), any(), any())).thenReturn(thingInfo);
        evergreenSetup =
                new EvergreenSetup(System.out, System.err, deviceProvisioningHelper, "--config", "mock_config_path", "--root",
                        "mock_root", "--thing-name", "mock_thing_name", "--policy-name", "mock_policy_name",
                        "--tes-role-name", "mock_tes_role_name", "--tes-role-alias-name", "mock_tes_role_alias_name",
                        "--provision", "y", "--setup-tes", "y", "--install-cli", "y", "--aws-region", "us-east-1");
        evergreenSetup.parseArgs();
        evergreenSetup.provision(kernel);
        verify(deviceProvisioningHelper, times(1)).createThing(any(), any(), any());
        verify(deviceProvisioningHelper, times(1)).updateKernelConfigWithIotConfiguration(any(), any(), any());
        verify(deviceProvisioningHelper, times(1)).setupIoTRoleForTes(any(), any(), any());
        verify(deviceProvisioningHelper, times(0)).createAndAttachRolePolicy(any(), any(), any());
        verify(deviceProvisioningHelper, times(1)).updateKernelConfigWithTesRoleInfo(any(), any());
    }

    @Test
    void GIVEN_setup_script_WHEN_script_is_used_with_short_arg_notations_THEN_setup_actions_are_performed()
            throws Exception {
        when(deviceProvisioningHelper.createThing(any(), any(), any())).thenReturn(thingInfo);
        evergreenSetup =
                new EvergreenSetup(System.out, System.err, deviceProvisioningHelper, "-i", "mock_config_path", "-r", "mock_root",
                        "-tn", "mock_thing_name", "-pn", "mock_policy_name", "-trn", "mock_tes_role_name", "-tra",
                        "mock_tes_role_alias_name", "-p", "y", "-t", "y", "-ic", "y", "-ar", "us-east-1",
                        "-trpn", "mock_policy_name", "-trpd", "mock_policy_doc_json");
        evergreenSetup.parseArgs();
        evergreenSetup.provision(kernel);
        verify(deviceProvisioningHelper, times(1)).createThing(any(), any(), any());
        verify(deviceProvisioningHelper, times(1)).updateKernelConfigWithIotConfiguration(any(), any(), any());
        verify(deviceProvisioningHelper, times(1)).setupIoTRoleForTes(any(), any(), any());
        verify(deviceProvisioningHelper, times(1)).createAndAttachRolePolicy(any(), any(), any());
        verify(deviceProvisioningHelper, times(1)).updateKernelConfigWithTesRoleInfo(any(), any());
    }

    @Test
    void GIVEN_setup_script_WHEN_script_is_used_with_unknown_args_THEN_script_fails(ExtensionContext context) {
        ignoreExceptionUltimateCauseWithMessage(context, "Undefined command line argument: -x");
        assertThrows(RuntimeException.class,
                () -> new EvergreenSetup(System.out, System.err, deviceProvisioningHelper, "-i", "mock_config_path", "-r",
                        "mock_root", "-tn", "mock_thing_name", "-x", "mock_wrong_arg_value", "-trn",
                        "mock_tes_role_name").parseArgs());
    }

    @Test
    void GIVEN_setup_script_WHEN_tes_policy_doc_missing_THEN_script_fails(ExtensionContext context) throws Exception {
        ignoreExceptionUltimateCauseWithMessage(context, "-trpn and -trpd must both be provided");
        assertThrows(RuntimeException.class,
                () -> new EvergreenSetup(System.out, System.err, deviceProvisioningHelper, "-i", "mock_config_path", "-r",
                        "mock_root", "-tn", "mock_thing_name", "-trpn", "mock_policy_name").parseArgs());
    }

    @Test
    void GIVEN_setup_script_WHEN_tes_usage_not_requested_THEN_tes_role_alias_is_not_setup() throws Exception {
        when(deviceProvisioningHelper.createThing(any(), any(), any())).thenReturn(thingInfo);
        evergreenSetup =
                new EvergreenSetup(System.out, System.err, deviceProvisioningHelper, "--config", "mock_config_path", "--root",
                        "mock_root", "--thing-name", "mock_thing_name", "--policy-name", "mock_policy_name",
                        "--tes-role-name", "mock_tes_role_name", "--tes-role-alias-name", "mock_tes_role_alias_name",
                        "--provision", "y", "--setup-tes", "n", "--install-cli", "y", "--aws-region", "us-east-1");
        evergreenSetup.parseArgs();
        evergreenSetup.provision(kernel);
        verify(deviceProvisioningHelper, times(1)).createThing(any(), any(), any());
        verify(deviceProvisioningHelper, times(1)).updateKernelConfigWithIotConfiguration(any(), any(), any());
        verify(deviceProvisioningHelper, times(0)).setupIoTRoleForTes(any(), any(), any());
        verify(deviceProvisioningHelper, times(0)).updateKernelConfigWithTesRoleInfo(any(), any());
    }

    @Test
    void GIVEN_setup_script_WHEN_dry_run_THEN_kernel_not_launched() throws Exception {
        evergreenSetup =
                new EvergreenSetup(System.out, System.err, deviceProvisioningHelper, "--config", "mock_config_path", "--root",
                        "mock_root", "--start", "false");

        EvergreenSetup evergreenSetupSpy = spy(evergreenSetup);
        doReturn(kernel).when(evergreenSetupSpy).getKernel();
        evergreenSetupSpy.parseArgs();
        evergreenSetupSpy.performSetup();
        verify(kernel, times(0)).launch();
    }
}
