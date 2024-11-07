#include <iostream>
#include "miracl/big.h"
 
Miracl precison(100,0); //必须添加，具体可见big.h中的说明
 
int main() {
    Big t("1002634737457474574485685858568568556856856");
    Big x("1052352352524634637457475685686690");
    Big y = t + x;
    std::cout << y << std::endl;
    return 0;
}
