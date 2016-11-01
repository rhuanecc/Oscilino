#define bufferSize 180
#define pinCh1 A0

typedef struct Buff{
  byte ch;  //canal
  unsigned int point[bufferSize];  //16b cada ponto
  unsigned long cks;       //32b 
} buff;

int i;
buff b;

void setup() {
  pinMode(pinCh1, INPUT);
  
  i = 0;
  b.ch = 0;

  Serial.begin(230400);    //hardware serial, pinos 0,1 (bluetooth)
  
  //Set mux pin -> A0
  ADMUX = 0b01000000;   //[2] AREF 5V, right ajust, [5] ADC0 pin  
  bitSet(ADCSRA,ADEN);  //Enable ADC

  delay(1000);   
}

void loop() { 
  if(i==0){
    bitSet(ADCSRA, ADSC);               //Start 1st sample conversion   
    
    Serial.write(b.ch);                 //Send 1st byte -> channel
    
    while (bit_is_set(ADCSRA, ADSC));   //Wait until conversion ends
    
    b.point[i] = ADCL | (ADCH << 8);    //Save conversion result
    //b.point[i] = i;
    b.cks += b.point[i];                //add checksum
    i++; 
  }
  
  bitSet(ADCSRA, ADSC);                 //Start sample conversion [1...n]
  
  //Send previous sample while converting ~> 100us
  Serial.write(b.point[i-1] >> 8);     //send 8 msb
  Serial.write(b.point[i-1]);          //send 8 lsb
  
  while (bit_is_set(ADCSRA, ADSC));   //Wait until conversion ends  
  
  b.point[i] = ADCL | (ADCH << 8);    //Save conversion result
  //b.point[i] = i;
  b.cks += b.point[i];                //add checksum
  
  i++;  
  
  if(i==bufferSize){      //se buffer cheio    
    //envia ultimo ponto
    Serial.write(b.point[i-1] >> 8);     //envia 8b mais significativos
    Serial.write(b.point[i-1]);          //envia 8b menos
    
    //envia checksum
    Serial.write(",");
    Serial.write(b.cks >> 24);  
    Serial.write(b.cks >> 16);  
    Serial.write(b.cks >> 8);     
    Serial.write(b.cks);        //checksum
    Serial.write(";");          //fim do buffer

    //limpa buffer - inicia nova serie de dados
    i=0;
    b.cks=0; 
    
    Serial.flush();          //aguarda fim do envio serial ~360us
  } 
}


