/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
#include <string.h>
#include <jni.h>
#include <android/log.h>

/* This is a trivial JNI example where we use a native method
 * to return a new VM String. See the corresponding Java source
 * file located at:
 *
 *   apps/samples/hello-jni/project/src/com/example/hellojni/HelloJni.java
 */

#include "engine_define.h"

#define TAG "MScript" // 这个是自定义的LOG的标识
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,TAG ,__VA_ARGS__) // 定义LOGD类型
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,TAG ,__VA_ARGS__) // 定义LOGI类型
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN,TAG ,__VA_ARGS__) // 定义LOGW类型
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,TAG ,__VA_ARGS__) // 定义LOGE类型
#define LOGF(...) __android_log_print(ANDROID_LOG_FATAL,TAG ,__VA_ARGS__) // 定义LOGF类型

OPCode codes[NUM_CODES];
int commReg[NUM_COMMREG];
int constReg[NUM_CONSTREG];
int constRegCount =0;
int PC,scriptSize;

enum KEYWORD{
    KY_NULL=0,
    KY_ADD,
    KY_SUB,
    KY_MULTI,
    KY_DIVIDE,

    KY_IF,
    KY_ELSE,
    KY_FOR,
    KY_WHILE,
    KY_END,
    KY_EQUAL,

    KY_ISEQUAL,
    KY_ISLESS,
    KY_ISBIG,
    KY_ISLESSEQUAL,
    KY_ISBIGEQUAL,

    KY_INPUT,
    KY_OUTPUT,
    KY_HIGH,
    KY_LOW,

    KY_PORT_NULL, // 20
    KY_PORT_1,
    KY_PORT_2,
    KY_PORT_3,
    KY_PORT_4,
    KY_PORT_5,
    KY_PORT_6,
    KY_PORT_7,
    KY_PORT_8,
    KY_PORT_M1,
    KY_PORT_M2,
    KY_SLOT1,
    KY_SLOT2,

    KY_FUNCTION,
    KY_DELAY,
    KY_PRINT,

    KY_PINMODE,
    KY_DWRITE,
    KY_DREAD,
    KY_AREAD,
    KY_SERVO_ATTACH,
    KY_SERVO_RUN,
    KY_DCRUN,
    KY_DISTANCE,
    KY_RGB_RUN,
    KY_DIGISEG,
    KY_SOUNDSENSOR,
    KY_LIGHTSENSOR,
    KY_LINEFINDER,
    KY_TEMPERATURE,
    KY_PIRSENSOR,
    KY_POTENTIOMETER,
    KY_LIMITSWITCH,
    KY_BUTTON,
    KY_VERSION,


    // device type
    KY_NORMAL, // the normal register value
    KY_ULTRASONIC,
    KY_ENCODER,

    KY_ALPHA,
    KY_DIGIT,

    DBG_LSC, // list codes
    DBG_LSR, // list registers
    DBG_LSN, // list consts

};

enum BLOCK{
    BK_NORMAL=0,
    BK_IF,
    BK_FOR,
    BK_WHILE,
};

typedef struct{
    char name[16];
}REGName;

typedef struct{
    char type;
    int startLine;
    int elseLine;
    int endLine;
    char forReg;
}CodeBlock;

REGName regList[16];

int blockIndex = 0;
CodeBlock block[5];

int regAlloc(char * name)
{
    int i;
    for(i=0;i<16;i++){
        if(strlen(regList[i].name)==0){
            memcpy(regList[i].name,name,strlen(name));
            return i;
        }
    }
    return -1;
}

int regGet(char * name)
{
    int i;
    for(i=0;i<16;i++){
        if(strcmp(regList[i].name,name)==0){
            return i;
        }
    }
    return -1;
}

int checkRegExist(char * a, int enableAlloc)
{
    int reg = regGet(a);
    if(reg==-1){
        if(enableAlloc){
            reg = regAlloc(a);
        }
    }
    return reg;
}

int checkConstExist(char * a, int enableAlloc)
{
    int i,ret;
    int v = atoi(a);
    for(i=0;i<constRegCount;i++){
        if(constReg[i] == v){
            return i;
        }
    }
    if(enableAlloc){
        ret = constRegCount;
        constReg[constRegCount++] = v;
        return ret;
    }
    return -1;
}


void regLs()
{
    int i;
    for(i=0;i<16;i++){
        if(strlen(regList[i].name)!=0){
            //CCLog("%d:%s\n",i,regList[i].name);
        	LOGD("%d:%s\n",i,regList[i].name);
        }
    }
}

int checkKeyWord(char * str)
{
    if(0==strcmp(str,"+")){
        return KY_ADD;
    }
    else if(0==strcmp(str,"-")){
        return KY_SUB;
    }
    else if(0==strcmp(str,"*")){
        return KY_MULTI;
    }
    else if(0==strcmp(str,"/")){
        return KY_DIVIDE;
    }
    else if(0==strcmp(str,"if")){
        return KY_IF;
    }
    else if(0==strcmp(str,"else")){
        return KY_ELSE;
    }
    else if(0==strcmp(str,"end")){
        return KY_END;
    }
    else if(0==strcmp(str,"while")){
        return KY_WHILE;
    }
    else if(0==strcmp(str,"for")){
        return KY_FOR;
    }
    else if(0==strcmp(str,"=")){
        return KY_EQUAL;
    }
    else if(0==strcmp(str,"==")){
        return KY_ISEQUAL;
    }
    else if(0==strcmp(str,"<")){
        return KY_ISLESS;
    }
    else if(0==strcmp(str,"<=")){
        return KY_ISLESSEQUAL;
    }
    else if(0==strcmp(str,">=")){
        return KY_ISBIGEQUAL;
    }
    else if(0==strcmp(str,">")){
        return KY_ISBIG;
    }
    else if(0==strcmp(str,"port1")){
        return KY_PORT_1;
    }
    else if(0==strcmp(str,"port2")){
        return KY_PORT_2;
    }
    else if(0==strcmp(str,"port3")){
        return KY_PORT_3;
    }
    else if(0==strcmp(str,"port4")){
        return KY_PORT_4;
    }
    else if(0==strcmp(str,"port5")){
        return KY_PORT_5;
    }
    else if(0==strcmp(str,"port6")){
        return KY_PORT_6;
    }
    else if(0==strcmp(str,"port7")){
        return KY_PORT_7;
    }
    else if(0==strcmp(str,"port8")){
        return KY_PORT_8;
    }
    else if(0==strcmp(str,"m1")){
        return KY_PORT_M1;
    }
    else if(0==strcmp(str,"m2")){
        return KY_PORT_M2;
    }
    else if(0==strcmp(str,"slot1")){
        return KY_SLOT1;
    }
    else if(0==strcmp(str,"slot2")){
        return KY_SLOT2;
    }
    else if(0==strcmp(str,"input")){
        return KY_INPUT;
    }
    else if(0==strcmp(str,"output")){
        return KY_OUTPUT;
    }
    else if(0==strcmp(str,"HIGH")){
        return KY_HIGH;
    }
    else if(0==strcmp(str,"LOW")){
        return KY_LOW;
    }

    if(strstr(str,"(")>0){
        return KY_FUNCTION;
    }

    if(0==strcmp(str,"dwrite")){
        return KY_DWRITE;
    }else if(0==strcmp(str,"dread")){
        return KY_DREAD;
    }else if(0==strcmp(str,"aread")){
        return KY_AREAD;
    }else if(0==strcmp(str,"dcrun")){
        return KY_DCRUN;
    }else if(0==strcmp(str,"distance")){
        return KY_DISTANCE;
    }else if(0==strcmp(str,"print")){
        return KY_PRINT;
    }else if(0==strcmp(str,"servoattach")){
        return KY_SERVO_ATTACH;
    }else if(0==strcmp(str,"servorun")){
        return KY_SERVO_RUN;
    }else if(0==strcmp(str,"rgbrun")){
        return KY_RGB_RUN;
    }else if(0==strcmp(str,"digiseg")){
        return KY_DIGISEG;
    }else if(0==strcmp(str,"soundsensor")){
        return KY_SOUNDSENSOR;
    }else if(0==strcmp(str,"lightsensor")){
        return KY_LIGHTSENSOR;
    }else if(0==strcmp(str,"linefinder")){
        return KY_LINEFINDER;
    }else if(0==strcmp(str,"temperature")){
        return KY_TEMPERATURE;
    }else if(0==strcmp(str,"pirsensor")){
        return KY_PIRSENSOR;
    }else if(0==strcmp(str,"potentiometer")){
        return KY_POTENTIOMETER;
    }else if(0==strcmp(str,"limitswitch")){
        return KY_LIMITSWITCH;
    }else if(0==strcmp(str,"button")){
        return KY_BUTTON;
    }else if(0==strcmp(str,"version")){
        return KY_VERSION;
    }


    if(0==strcmp(str,"normal")){
        return KY_NORMAL;
    }else if(0==strcmp(str,"ultrasonic")){
        return KY_ULTRASONIC;
    }else if(0==strcmp(str,"encoder")){
        return KY_ENCODER;
    }

    if(isdigit(str[0])){
        return KY_DIGIT;
    }else if(isalpha(str[0])){
        return KY_ALPHA;
    }
    //CCLog("--ERROR-- unknow keyword %s",str);
    LOGD("--ERROR-- unknow keyword %s",str);
    return KY_NULL;
}


// build in functions for makeblock
void buildCode(int pc, char sp, char op, char a, char b)
{
    codes[pc].op = op+(sp<<6);
    codes[pc].v = (a<<4) + b;
}

void cpDWrite(char port, char slot, char value)
{
    slot = slot - KY_SLOT1;
    port = port - KY_PORT_NULL;
    if(value == KY_HIGH){
        buildCode(PC,slot,OP_PORT_DWRITE,port,HIGH);
    }else{
        buildCode(PC,slot,OP_PORT_DWRITE,port,LOW);
    }
    PC++;
}

void cpDRead(char port, char retReg)
{
    port = port - KY_PORT_NULL;
    buildCode(PC,0,OP_PORT_DREAD,port,retReg);
    PC++;
}

void cpARead(char port, char slot, char retReg)
{
    slot = slot - KY_SLOT1;
    port = port - KY_PORT_NULL;
    buildCode(PC,slot,OP_PORT_AREAD,port,retReg);
    PC++;
}

void cpDistance(char port, char retReg)
{
    port = port - KY_PORT_NULL;
    buildCode(PC,0,OP_PORT_DISTANCE,port,retReg);
    PC++;
}

void cpPrint(char type, char * reg, char * index)
{
    int regInt = regGet(reg);
    int indexInt = atoi(index);
    if(type==KY_NORMAL){
        type = PRINT_NORMAL;
    }else if(type==KY_ULTRASONIC){
        type = PRINT_ULTRASONIC;
    }else if(type==KY_ENCODER){
        type = PRINT_ENCODER;
    }else if(type==KY_TEMPERATURE){
        type = PRINT_TEMPERATURE;
    }else if(type==KY_LIGHTSENSOR){
        type = PRINT_LIGHTSENSOR;
    }else if(type==KY_SOUNDSENSOR){
        type = PRINT_SOUNDSENSOR;
    }else if(type==KY_LINEFINDER){
        type = PRINT_LINEFINDER;
    }else if(type==KY_POTENTIOMETER){
        type = PRINT_POTENTIOMETER;
    }else if(type==KY_LIMITSWITCH){
        type = PRINT_LIMITSWITCH;
    }else if(type==KY_PIRSENSOR){
        type = PRINT_PIRSENSOR;
    }else if(type==KY_BUTTON){
        type = PRINT_BUTTON;
    }else if(type==KY_VERSION){
        type = PRINT_VERSION;
    }
    buildCode(PC,indexInt,OP_PRINT,regInt,type);
    PC++;
}

void cpDCRun(char port, char * value)
{
    char sp=0;
    char valueReg;
    codes[PC].op = OP_PORT_DCRUN;
    if(checkKeyWord(value)==KY_DIGIT){
        sp = 1;
        valueReg = checkConstExist(value,1);
    }else{
        valueReg = checkRegExist(value,1);
    }
    port = port - KY_PORT_NULL;
    buildCode(PC,sp,OP_PORT_DCRUN,port,valueReg);
    PC++;
}

void cpServoAttach(char port, char index, char slot)
{

    slot = slot - KY_SLOT1;
    port = port - KY_PORT_NULL;
    buildCode(PC,slot,OP_PORT_SERVOATTACH,port,index);
    PC++;
}

void cpServoRun(char index, char * value)
{
    char sp=0;
    char valueReg;
    if(checkKeyWord(value)==KY_DIGIT){
        sp = 1;
        valueReg = checkConstExist(value,1);
    }else{
        valueReg = checkRegExist(value,1);
    }
    buildCode(PC,sp,OP_PORT_SERVORUN,valueReg,index);
    PC++;
}

void cpRGBRun(char port, char slot, char * reg)
{
    char valueReg;
    valueReg = checkRegExist(reg,1);
    slot = slot - KY_SLOT1;
    port = port - KY_PORT_NULL;
    buildCode(PC,slot,OP_PORT_RGBRUN,port,valueReg);
    PC++;
}

void cpDigiSegRun(char port, char * reg)
{
    char valueReg,sp=0;
    valueReg = checkRegExist(reg,1);
    port = port - KY_PORT_NULL;
    buildCode(PC,sp,OP_PORT_DIGISEG,port,valueReg);
    PC++;
}

void cpTemperature(char port, char slot, char retReg)
{
    slot = slot - KY_SLOT1;
    port = port - KY_PORT_NULL;
    buildCode(PC,slot,OP_PORT_TEMPERATURE,port,retReg);
    PC++;
}


void cpIncrease(int reg)
{
    codes[PC].op = OP_INCREASE;
    codes[PC].v= (reg<<4);
    PC++;
}

void cpJMP(int pos, int target, char jumpType)
{
    //Serial.printf("parse jmp pos=%d tar=%d\n",pos,target);
    char sp=0;
    int delta = target - pos;
    if(delta<0){
        delta = -delta;
        sp = 1; // jump back
    }
    codes[pos].op = jumpType;
    codes[pos].op += (sp<<6);
    codes[pos].v = delta;
}

void cpLogic(char * a,char * b,char op)
{
    int aN,bN;
    char sp=0;
    if(op == KY_ISEQUAL){
        op = OP_ISEQUAL;
    }else if(op == KY_ISLESS){
        op = OP_ISLESS;
    }else if(op == KY_ISBIG){
        op = OP_ISBIG;
    }else if(op == KY_ISLESSEQUAL){
        op = OP_ISLESSEQUAL;
    }else if(op == KY_ISBIGEQUAL){
        op = OP_ISBIGEQUAL;
    }
    codes[PC].op = op;
    if(checkKeyWord(a)==KY_DIGIT){
        aN = checkConstExist(a,1);
        sp+=2;
    }else{
        aN = checkRegExist(a,1);
    }
    if(checkKeyWord(b)==KY_DIGIT){
        bN = checkConstExist(b,1);
        sp+=1;
    }else{
        bN = checkRegExist(b,1);
    }
    codes[PC].op += (sp<<6);
    codes[PC].v = (aN<<4) + bN;
    PC++;
}

void cpArith(char * a, char * b, char op){
    int aN,bN;
    char sp=0;
    if(op==KY_ADD){
        op=OP_ADD;
    }else if(op==KY_SUB){
        op = OP_SUB;
    }else if(op==KY_MULTI){
        op = OP_MULTI;
    }else if(op==KY_DIVIDE){
        op = OP_DIVIDE;
    }
    codes[PC].op = op;
    if(checkKeyWord(a)==KY_DIGIT){
        aN = checkConstExist(a,1);
        sp+=2;
    }else{
        aN = checkRegExist(a,1);
    }

    if(checkKeyWord(b)==KY_DIGIT){
        bN = checkConstExist(b,1);
        sp+=1;
    }else{
        bN = checkRegExist(b,1);
    }
    codes[PC].op += (sp<<6);
    codes[PC].v = (aN<<4) + bN;
    PC++;
}

void cpEqual(char * left, char * right, int from)
{
    int leftN,rightN;
    leftN = checkRegExist(left,1);
    codes[PC].op = OP_EQUAL;
    codes[PC].v = (leftN<<4);
    if(from==1){
        rightN = 0;
        codes[PC].op += (2<<6);
    }else if(checkKeyWord(right)==KY_ALPHA){
        rightN = checkRegExist(right,1);
    }else if(checkKeyWord(right)==KY_DIGIT){
        rightN = checkConstExist(right,1);
        codes[PC].op += (1<<6);
    }
    codes[PC].v += (rightN & 0x0f);
    PC++;
}


void bkPush(char type)
{
    blockIndex++;
    block[blockIndex].type = type;
    block[blockIndex].startLine = PC;
}

void bkPop()
{
    memset(&block[blockIndex],0,sizeof(CodeBlock));
    blockIndex--;
}



void parseFunction(char * str, char retReg)
{
    int funType,n=0;
    char * funName;
    char * tmp,*tmp2;
    char * a[5];
    char kw[5];
    funName = strtok_r(str, "(", &tmp);
    funType = checkKeyWord(funName);
    tmp2 = strstr(tmp,")");
    *tmp2 = '\0';
    while ((a[n] = strtok_r(0, ",", &tmp)) != 0) {
        kw[n] = checkKeyWord(a[n]);
        //Serial.printf("%s %d\n",a[n],kw[n]);
        n++;
    }
    if(funType==KY_DWRITE){
        cpDWrite(kw[0],kw[1],kw[2]);
    }else if(funType==KY_DREAD){
        cpDRead(kw[0],retReg);
    }else if(funType==KY_AREAD){
        cpARead(kw[0],kw[1],retReg);
    }else if(funType==KY_DCRUN){
        cpDCRun(kw[0],a[1]);
    }else if(funType==KY_DISTANCE){
        cpDistance(kw[0],retReg);
    }else if(funType==KY_PRINT){
        cpPrint(kw[0],a[1],a[2]);
    }else if(funType==KY_SERVO_ATTACH){
        int index = atoi(a[2]);
        cpServoAttach(kw[0], index, kw[1]); // todo: add servo index list
    }else if(funType==KY_SERVO_RUN){
        int index = atoi(a[0]);
        cpServoRun(index,a[1]);
    }else if(funType==KY_RGB_RUN){
        cpRGBRun(kw[0],kw[1],a[2]);
    }else if(funType==KY_DIGISEG){
        cpDigiSegRun(kw[0],a[1]);
    }else if(funType==KY_SOUNDSENSOR){
        cpARead(kw[0],KY_SLOT2,retReg);
    }else if(funType==KY_LIGHTSENSOR){
        cpARead(kw[0],KY_SLOT2,retReg);
    }else if(funType==KY_TEMPERATURE){
        cpTemperature(kw[0],kw[1],retReg);
    }else if(funType==KY_LINEFINDER){
        cpDRead(kw[0], retReg);
    }else if(funType==KY_PIRSENSOR){
        cpDRead(kw[0], retReg);
    }else if(funType==KY_POTENTIOMETER){
        cpARead(kw[0],KY_SLOT2,retReg);
    }else if(funType==KY_LIMITSWITCH){
        cpDRead(kw[0], retReg);
    }else if(funType==KY_BUTTON){
        cpARead(kw[0],KY_SLOT2,retReg);
    }else{
        //CCLog("--ERROR-- unknow function %d",funType);
    	LOGD("--ERROR-- unknow function %d",funType);
    }
}

void parseLine(char * line)
{
    char * tmp;
    char * a[10];
    char kw[10];
    a[0] = strtok_r(line, " ", &tmp);
    kw[0] = checkKeyWord(a[0]);
    int n = 1;
    while ((a[n] = strtok_r(0, " ", &tmp)) != 0) {
        kw[n] = checkKeyWord(a[n]);
        n++;
    }
    //Serial.printf("n=%d\n",n);
    if(n==3 && kw[0]==KY_ALPHA && kw[1]==KY_EQUAL){
        if(kw[2]==KY_FUNCTION){
            char retReg = checkRegExist(a[0],1);
            parseFunction(a[2],retReg);
        }else{
            cpEqual(a[0],a[2],0);
        }
    }else if(n==5 && kw[0]==KY_ALPHA && kw[1]==KY_EQUAL && kw[3]<=KY_DIVIDE){
        // do arith calc first
        cpArith(a[2],a[4],kw[3]);
        // return arithRegister value
        cpEqual(a[0],0,1);
    }else if(kw[0]==KY_IF){
        //Serial.printf("parse if\n");
        bkPush(BK_IF);  // if a == b
        cpLogic(a[1],a[3],kw[2]);
        PC++;// reserve a pc slot for jumper
    }else if(kw[0]==KY_WHILE){
        bkPush(BK_WHILE);  // while a == b
        cpLogic(a[1],a[3],kw[2]);
        PC++; // reserve slot
    }else if(kw[0]==KY_FOR){
        // for a = b to c (a<=c)
        //Serial.printf("parse for\n");
        cpEqual(a[1],a[3],0);// a = b
        bkPush(BK_FOR);
        cpLogic(a[1],a[5],KY_ISLESSEQUAL); // a,=d
        block[blockIndex].forReg = checkRegExist(a[1],1);
        PC++;// reserve slot
    }else if(kw[0]==KY_ELSE){ // only if has else
        block[blockIndex].elseLine = PC;
        PC++; // reserve this slot
    }else if(kw[0]==KY_END){
        block[blockIndex].endLine = PC;
        if(block[blockIndex].type==BK_IF){
            if(block[blockIndex].elseLine!=0){
                cpJMP(block[blockIndex].startLine+1,block[blockIndex].elseLine+1,OP_JNMP); // jump to else at startpos
                cpJMP(block[blockIndex].elseLine,block[blockIndex].endLine,OP_JMP); // jump to end at else
            }else{
                cpJMP(block[blockIndex].startLine+1,block[blockIndex].endLine,OP_JNMP); // jump to end pos
            }
            bkPop();
        }else if(block[blockIndex].type==BK_WHILE){
            cpJMP(block[blockIndex].startLine+1,block[blockIndex].endLine+1,OP_JNMP); // jump out of loop
            cpJMP(block[blockIndex].endLine,block[blockIndex].startLine,OP_JMP); // jump back to logic control
            PC++;
            bkPop();
        }else if(block[blockIndex].type==BK_FOR){
            cpIncrease(block[blockIndex].forReg);
            cpJMP(block[blockIndex].startLine+1,block[blockIndex].endLine+2,OP_JNMP); // jump out of loop
            cpJMP(block[blockIndex].endLine+1,block[blockIndex].startLine,OP_JMP); // jump out of loop
        }
    }else if(kw[0]==KY_FUNCTION){
        parseFunction(a[0],-1);
    }
}

void compilerReset()
{
    memset(codes, 0, NUM_CODES*sizeof(OPCode));
    constRegCount = 0;
    blockIndex = 0;
    PC = 0;
}

char parseBuff[64];
void compile(char * code)
{
    int len;
    char * ptr  =code;
    char * line = ptr;
    len = strlen(code);
    compilerReset();
    // find '\n' and parse
    while((ptr-code)<len){
        if(*ptr=='\n'){
            *ptr = 0;
            memcpy(parseBuff, line, strlen(line));
            //CCLog("parse %s",parseBuff);
            LOGD("parse %s",parseBuff);
            parseLine(parseBuff);
            memset(parseBuff,0,64);
            line = ptr+1;
        }else{
            ptr++;
        }
    }
}

void listCode(void)
{
    char op, sp, a, b;
    int i;
    for(i = 0;i<=PC;i++){
        op = codes[i].op & 0x3f;
        sp = codes[i].op>>6;
        a = (codes[i].v >> 4);
        b = (codes[i].v & 0x0f);
        //CCLog("L %d: %d, %d, %d, %d", i, sp, op, a, b);
        LOGD("L %d: %d, %d, %d, %d", i, sp, op, a, b);
    }

}

void listCommReg(void)
{
    int i;
    for(i = 0;i<16;i++){
        if(regList[i].name[0]==0){
            continue;
        }
        //CCLog("R %d: %s %d", i, regList[i].name, commReg[i]);
        LOGD("R %d: %s %d", i, regList[i].name, commReg[i]);
    }
}

void listConstReg(void)
{
    int i;
    for(i = 0;i<constRegCount;i++){
        //CCLog("N %d: %d", i, constReg[i]);
    	LOGD("N %d: %d", i, constReg[i]);
    }
}

int getCodeCount()
{
    return PC;
}

int getConstRegCount()
{
    return constRegCount;
}

void getCode(int index, OPCode * opc)
{
    opc->op =codes[index].op;
    opc->v =codes[index].v;
}

int getConstReg(int index)
{
    return constReg[index];
}

int getRegIndex(const char * name)
{
    int i;
    for(i=0;i<NUM_COMMREG;i++){
        if(strcmp(regList[i].name,name)==0){
            return i;
        }
    }
    return -1;
}

char retBuf[128];
jstring Java_cc_makeblock_makeblock_MScript_getIrqJNI( JNIEnv* env,
        jobject thiz, jstring msg)
{
    int PCI = PC;
    PC = 400;
    char * c_code = (*env)->GetStringUTFChars(env,msg,0);
    memcpy(parseBuff, c_code, strlen(c_code));
    parseLine(parseBuff);
	memset(parseBuff,0,64);
	PC = PCI;
	memset(retBuf,0,128);
	sprintf(retBuf,"I:%x,%x\n",codes[400].op,codes[400].v);
    return (*env)->NewStringUTF(env, retBuf);
}

jstring Java_cc_makeblock_makeblock_MScript_getRegName(JNIEnv* env,
        jobject thiz, jint index)
{
	//memset(retBuf,0,128);
	//sprintf(retBuf,"N:%1d,%1d\n",index,constReg[index]);
	return (*env)->NewStringUTF(env, regList[index].name);
}

jint Java_cc_makeblock_makeblock_MScript_getRegIndex(JNIEnv* env,
        jobject thiz, jstring msg)
{
	char * c_code = (*env)->GetStringUTFChars(env,msg,0);
	int regindex = regGet(c_code);
	return regindex;
}

jstring Java_cc_makeblock_makeblock_MScript_getConstJNI(JNIEnv* env,
        jobject thiz, jint index)
{
	memset(retBuf,0,128);
	sprintf(retBuf,"N:%1d,%1d\n",index,constReg[index]);
    return (*env)->NewStringUTF(env, retBuf);
}

jstring Java_cc_makeblock_makeblock_MScript_getCodeJNI(JNIEnv* env,
        jobject thiz, jint index)
{
	memset(retBuf,0,128);
	sprintf(retBuf,"C %d:%1x,%1x\n",index,codes[index].op,codes[index].v);
    return (*env)->NewStringUTF(env, retBuf);
}

jstring Java_cc_makeblock_makeblock_MScript_compileJNI( JNIEnv* env,
        jobject thiz, jstring msg)
{
	char* c_code = NULL;
	c_code = (*env)->GetStringUTFChars(env,msg,0);
	compile(c_code);
	//listCode();
	//listCommReg();
	//listConstReg();
	int numofcode = PC+constRegCount;
	sprintf(retBuf,"done %d %d\n",PC,constRegCount);
    return (*env)->NewStringUTF(env, retBuf);
}

jstring Java_cc_makeblock_makeblock_MScript_stringFromJNI( JNIEnv* env,
                                                  jobject thiz )
{
#if defined(__arm__)
  #if defined(__ARM_ARCH_7A__)
    #if defined(__ARM_NEON__)
      #define ABI "armeabi-v7a/NEON"
    #else
      #define ABI "armeabi-v7a"
    #endif
  #else
   #define ABI "armeabi"
  #endif
#elif defined(__i386__)
   #define ABI "x86"
#elif defined(__mips__)
   #define ABI "mips"
#else
   #define ABI "unknown"
#endif

    return (*env)->NewStringUTF(env, "Hello from JNI !  Compiled with ABI " ABI ".");
}


