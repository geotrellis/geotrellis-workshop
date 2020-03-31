FROM node:lts

WORKDIR /app/website

EXPOSE 3000 35729
COPY ./target/mdoc /app/target/mdoc
COPY ./website /app/website
RUN yarn install

CMD ["yarn", "start"]
