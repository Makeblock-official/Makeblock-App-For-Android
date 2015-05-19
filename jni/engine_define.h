/*
 * engine_define.h
 *
 *  Created on: 2014-6-27
 *      Author: RivenYeung
 */

#ifndef ENGINE_DEFINE_H_
#define ENGINE_DEFINE_H_
#define NUM_CODES  420 // 480
#define NUM_COMMREG 16
#define NUM_CONSTREG 16
#define NUM_PROCSTACK 4

#define HIGH 0x1
#define LOW  0x0

#define INPUT 0x0
#define OUTPUT 0x1
#define INPUT_PULLUP 0x2

  #define NC 					-1

#define PORT_1 				0x01
#define PORT_2 				0x02
#define PORT_3 				0x03
#define PORT_4 				0x04
#define PORT_5 				0x05
#define PORT_6 				0x06
#define PORT_7 				0x07
#define PORT_8 				0x08
#define M1     				0x09
#define M2     				0x0a

enum OP{
    OP_NULL = 0,
    OP_ADD,
    OP_SUB,
    OP_MULTI,
    OP_DIVIDE,
    OP_EQUAL,
    OP_PRINT,
    OP_ISEQUAL,
    OP_ISBIG,
    OP_ISLESS,
    OP_ISBIGEQUAL,
    OP_ISLESSEQUAL,
    OP_JMP, // just jump
    OP_JNMP, // jump if logic false
    OP_JMPC, // jump to a const reg specified postion
    OP_INCREASE, // ++ operation
    OP_PORT_PINMODE,
    OP_PORT_DWRITE,
    OP_PORT_DREAD,
    OP_PORT_AWRITE,
    OP_PORT_AREAD,
    OP_PORT_PULSEIN,
    OP_PORT_DCRUN, // dc moto run
    OP_PORT_SERVOATTACH,
    OP_PORT_SERVORUN,
    OP_PORT_DISTANCE, // ultra sonic read distance
    OP_PORT_TEMPERATURE,
    OP_PORT_RGBRUN,
    OP_PORT_DIGISEG,
    OP_WIRE_BEGIN,
    OP_WIRE_READ,
    OP_WIRE_WRITE,
    OP_TOP=63,
};

enum PrintType{
    PRINT_NORMAL = 0,
    PRINT_ULTRASONIC,
    PRINT_ENCODER,
    PRINT_TEMPERATURE,
    PRINT_LIGHTSENSOR,
    PRINT_SOUNDSENSOR,
    PRINT_LINEFINDER,
    PRINT_POTENTIOMETER,
    PRINT_LIMITSWITCH,
    PRINT_PIRSENSOR,
    PRINT_BUTTON,
    PRINT_VERSION,
};

typedef struct{
  unsigned char op;
  unsigned char v;
}OPCode;



#endif /* ENGINE_DEFINE_H_ */
