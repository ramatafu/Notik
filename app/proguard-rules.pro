# Room, Glance and reflection-based libraries are already handled by their
# own consumer-rules.pro bundled in the AARs — nothing extra is required
# for the current dependency set. Add app-specific keep rules here if you
# introduce reflection-based serialization (e.g. Gson/Moshi) later.

-keepattributes *Annotation*
