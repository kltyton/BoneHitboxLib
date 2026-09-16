package com.kltyton.bonehitboxlib.api.state.runtime;

import org.jetbrains.annotations.Nullable;

/** Internal entity attachment supplied by the common Entity mixin. */
public interface ObbEntityStateAccess {
    @Nullable ObbEntityState bonehitboxlib$getAttachedState();
    void bonehitboxlib$setAttachedState(ObbEntityState state);
}
