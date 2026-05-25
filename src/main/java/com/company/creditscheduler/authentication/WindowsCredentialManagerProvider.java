package com.company.creditscheduler.authentication;

import com.company.creditscheduler.dto.Credentials;
import com.company.creditscheduler.exception.CreditSchedulerException;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.WString;
import com.sun.jna.platform.win32.WinBase.FILETIME;
import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;
import com.sun.jna.win32.W32APITypeMapper;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class WindowsCredentialManagerProvider implements CredentialProvider {

    @Override
    public Credentials getCredentials(String targetName) {
        if (!Platform.isWindows()) {
            throw new CreditSchedulerException("Windows Credential Manager is only available on Windows hosts");
        }
        try {
            PointerByReference credentialPointer = new PointerByReference();
            boolean found = CredentialAdvapi32.INSTANCE.CredRead(new WString(targetName), 1, 0, credentialPointer);
            if (!found) {
                throw new CreditSchedulerException("Missing Windows Credential Manager credential: " + targetName);
            }
            Pointer pointer = credentialPointer.getValue();
            try {
                Credential credential = new Credential(pointer);
                byte[] bytes = credential.credentialBlob.getByteArray(0, credential.credentialBlobSize.intValue());
                String password = new String(bytes, StandardCharsets.UTF_16LE);
                return new Credentials(credential.userName, password.toCharArray());
            } finally {
                CredentialAdvapi32.INSTANCE.CredFree(pointer);
            }
        } catch (RuntimeException exception) {
            throw new CreditSchedulerException("Missing Windows Credential Manager credential: " + targetName, exception);
        }
    }

    private interface CredentialAdvapi32 extends StdCallLibrary {
        CredentialAdvapi32 INSTANCE = Native.load("Advapi32", CredentialAdvapi32.class, W32APIOptions.UNICODE_OPTIONS);

        boolean CredRead(WString targetName, int type, int flags, PointerByReference credential);

        void CredFree(Pointer credential);
    }

    @Structure.FieldOrder({
            "flags", "type", "targetName", "comment", "lastWritten", "credentialBlobSize",
            "credentialBlob", "persist", "attributeCount", "attributes", "targetAlias", "userName"
    })
    public static class Credential extends Structure {
        public DWORD flags;
        public DWORD type;
        public String targetName;
        public String comment;
        public FILETIME lastWritten;
        public DWORD credentialBlobSize;
        public Pointer credentialBlob;
        public DWORD persist;
        public DWORD attributeCount;
        public Pointer attributes;
        public String targetAlias;
        public String userName;

        public Credential(Pointer pointer) {
            super(pointer, ALIGN_DEFAULT, W32APITypeMapper.UNICODE);
            read();
        }

        @Override
        protected List<String> getFieldOrder() {
            return List.of(
                    "flags", "type", "targetName", "comment", "lastWritten", "credentialBlobSize",
                    "credentialBlob", "persist", "attributeCount", "attributes", "targetAlias", "userName");
        }
    }
}
