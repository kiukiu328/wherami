
#ifdef RCT_NEW_ARCH_ENABLED
#import "RNWheramiSpec.h"

@interface Wherami : NSObject <NativeWheramiSpec>
#else
#import <React/RCTBridgeModule.h>

@interface Wherami : NSObject <RCTBridgeModule>
#endif

@end
