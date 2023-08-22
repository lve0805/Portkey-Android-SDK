package io.aelf.portkey.init;

import io.aelf.portkey.storage.IStorageBehaviour;
import io.aelf.portkey.utils.log.ILogger;

public class SDkInitConfig {
    private final ILogger logger;
    private final IStorageBehaviour storageHandler;
    private final boolean useOutsideComposeStub;

    public SDkInitConfig(Builder builder) {
        this.logger = builder.logger;
        this.storageHandler = builder.storageHandler;
        this.useOutsideComposeStub = builder.useOutsideComposeStub;
    }

    public ILogger getLogger() {
        return logger;
    }

    public IStorageBehaviour getStorageHandler() {
        return storageHandler;
    }

    public boolean isUseOutsideComposeStub() {
        return useOutsideComposeStub;
    }

    public static class Builder {
        public Builder() {

        }
        private ILogger logger;
        private IStorageBehaviour storageHandler;
        public boolean useOutsideComposeStub;


        public Builder setLogger(ILogger logger) {
            this.logger = logger;
            return this;
        }

        public Builder setStorageHandler(IStorageBehaviour storageHandler) {
            this.storageHandler = storageHandler;
            return this;
        }

        public Builder setUseOutsideComposeStub(boolean useOutsideComposeStub) {
            this.useOutsideComposeStub = useOutsideComposeStub;
            return this;
        }

        public SDkInitConfig build() {
            return new SDkInitConfig(this);
        }
    }
}
