package nl.meandi.apns;

import javax.resource.ResourceException;
import javax.resource.spi.*;
import java.util.Objects;

@Activation(messageListeners = { ApnsListener.class })
public class ApnsActivationSpec implements ActivationSpec, ResourceAdapterAssociation {

    @ConfigProperty
    private String certificateFileName = ApnsResourceAdapter.DEFAULT_CERTIFICATE_FILE_NAME;

    @ConfigProperty(confidential = true)
    private String certificateFilePassword;

    private ApnsResourceAdapter resourceAdapter;

    public String getCertificateFileName() {
        return certificateFileName;
    }

    @SuppressWarnings("unused")
    public void setCertificateFileName(String certificateFileName) {
        this.certificateFileName = certificateFileName;
    }

    public String getCertificateFilePassword() {
        return certificateFilePassword;
    }

    @SuppressWarnings("unused")
    public void setCertificateFilePassword(String certificateFilePassword) {
        this.certificateFilePassword = certificateFilePassword;
    }

    @Override
    public void validate() throws InvalidPropertyException {
    }

    @Override
    public ResourceAdapter getResourceAdapter() {
        return resourceAdapter;
    }

    @Override
    public void setResourceAdapter(ResourceAdapter resourceAdapter) throws ResourceException {
        this.resourceAdapter = (ApnsResourceAdapter) resourceAdapter;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApnsActivationSpec that = (ApnsActivationSpec) o;
        return Objects.equals(certificateFileName, that.certificateFileName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(certificateFileName);
    }
}
